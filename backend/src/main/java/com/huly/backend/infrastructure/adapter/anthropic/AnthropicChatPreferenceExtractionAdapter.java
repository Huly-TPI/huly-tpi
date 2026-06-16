package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicChatPreferenceExtractionAdapter implements ChatPreferenceExtractionPort {

    private static final String SYSTEM_PROMPT = """
            Extraé únicamente preferencias conversacionales explícitas del usuario.
            No confundas saludos, lugares, emociones ni frases introductorias con nombres.
            Si el usuario dice su nombre real y luego indica cómo quiere que lo llamen, priorizá el alias solicitado.
            Los estilos permitidos son: NEUTRAL, SERIOUS, FORMAL, FRIENDLY, INFORMAL, CLOSE,
            FRIEND_LIKE, DIRECT, INDIRECT, GENTLE_SUPPORTIVE, MOTIVATIONAL, CONCISE_DIRECT.
            messageType debe ser PREFERENCE_ONLY si el mensaje sólo saluda o informa preferencias,
            MIXED si además contiene un tema que el chatbot debe responder, o UNRELATED si no hay preferencia.
            Respondé sólo JSON:
            {"preferredName":null,"communicationStyle":null,"messageType":"UNRELATED","confidence":0.0}
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicChatPreferenceExtractionAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatPreferenceDetectionResult extract(
            String message,
            ChatPreferenceExpectedField expectedField) {
        try {
            String request = "Campo esperado: " + expectedField + "\nMensaje: " + message;
            ChatResponse response = chatModel.call(new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(request))));
            String raw = response.getResult().getOutput().getText();
            JsonNode node = objectMapper.readTree(extractJson(raw));
            return new ChatPreferenceDetectionResult(
                    textOrNull(node, "preferredName"),
                    parseStyle(textOrNull(node, "communicationStyle")),
                    parseMessageType(textOrNull(node, "messageType")),
                    node.path("confidence").asDouble(0.0));
        } catch (Exception exception) {
            log.warn("No se pudo extraer la preferencia conversacional: {}", exception.getMessage());
            return ChatPreferenceDetectionResult.unrelated();
        }
    }

    private CommunicationStyle parseStyle(String value) {
        if (value == null) {
            return null;
        }
        try {
            return CommunicationStyle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private ChatPreferenceMessageType parseMessageType(String value) {
        if (value == null) {
            return ChatPreferenceMessageType.UNRELATED;
        }
        try {
            return ChatPreferenceMessageType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ChatPreferenceMessageType.UNRELATED;
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        return value.asText().trim();
    }

    private String extractJson(String raw) {
        if (raw == null) {
            return "{}";
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        return start >= 0 && end > start ? raw.substring(start, end + 1) : raw;
    }
}
