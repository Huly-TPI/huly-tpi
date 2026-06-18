package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
            Respondé sólo JSON (tu framework se encarga del formato exacto):
            """;

    private final ChatClient chatClient;

    public AnthropicChatPreferenceExtractionAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public ChatPreferenceDetectionResult extract(
            String message,
            ChatPreferenceExpectedField expectedField) {
        try {
            String request = "Campo esperado: " + expectedField + "\nMensaje: " + message;
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(request)
                    .call()
                    .entity(ChatPreferenceDetectionResult.class);
        } catch (Exception exception) {
            log.warn("No se pudo extraer la preferencia conversacional: {}", exception.getMessage());
            return ChatPreferenceDetectionResult.unrelated();
        }
    }
}
