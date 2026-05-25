package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.provider.LLMChatPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/**
 * Adaptador de chat para el proveedor Anthropic, implementa {@link LLMChatPort}
 * usando Spring AI para la comunicación con el modelo de lenguaje.
 *
 * <p>Se activa únicamente cuando la propiedad {@code app.ai.provider=anthropic}
 * está configurada en el entorno de la aplicación.
 */
@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicChatAdapter implements LLMChatPort {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicChatAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Construye la lista de mensajes con el prompt del sistema, el historial
     * y el mensaje actual, luego delega al modelo y parsea la respuesta JSON.
     */
    @Override
    public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = buildMessages(systemPrompt, userMessage, history);

        String raw = chatModel.call(new Prompt(messages))
                .getResult()
                .getOutput()
                .getText();

        return parseResponse(raw);
    }

    /**
     * Parsea la respuesta cruda del modelo intentando extraer un JSON estructurado.
     *
     * <p>Si el parseo falla por cualquier motivo, se registra una advertencia
     * y se retorna un {@link ChatReply} con el texto plano como contenido.
     *
     * @param raw texto crudo devuelto por el modelo de lenguaje
     * @return {@link ChatReply} con los campos extraídos o con el texto plano en caso de error
     */
    private ChatReply parseResponse(String raw) {
        try {
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);

            String reply = node.path("huly_reply").asText(raw);
            EmotionType emotion = parseEmotion(node.path("detected_emotion").asText(null));
            Integer intensity = extractIntOrNull(node, "intensity");
            Boolean riskDetected = extractBooleanOrNull(node, "risk_detected");
            String matchedWord = node.path("matched_word").isNull()
                    ? null : node.path("matched_word").asText(null);

            return new ChatReply(reply, emotion, intensity, riskDetected, matchedWord);
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta estructurada, usando texto plano");
            return ChatReply.of(raw);
        }
    }

    /**
     * Extrae el bloque JSON de un texto que puede contener contenido adicional
     * antes o después del JSON.
     *
     * <p>Busca el primer {@code '{'} y el último {@code '}'} para delimitar el JSON.
     * Si no se encuentran los delimitadores, retorna el texto original.
     *
     * @param raw texto crudo del cual extraer el JSON
     * @return cadena JSON extraída, o el texto original si no se encontraron delimitadores
     */
    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    /**
     * Parsea un valor de texto al enum {@link EmotionType} correspondiente.
     *
     * <p>Normaliza el valor a mayúsculas antes de intentar la conversión.
     * Retorna {@code null} si el valor es nulo, vacío o no corresponde a ningún enum.
     *
     * @param value texto que representa la emoción detectada
     * @return {@link EmotionType} correspondiente, o {@code null} si no es válido
     */
    private EmotionType parseEmotion(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return EmotionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extrae un valor entero de un nodo JSON, retornando {@code null}
     * si el campo no existe o es nulo.
     *
     * @param node      nodo JSON del cual extraer el valor
     * @param fieldName nombre del campo a extraer
     * @return valor entero del campo, o {@code null} si no existe o es nulo
     */
    private Integer extractIntOrNull(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.path(fieldName).isNull()
                ? node.path(fieldName).asInt()
                : null;
    }

    /**
     * Extrae un valor booleano de un nodo JSON, retornando {@code null}
     * si el campo no existe o es nulo.
     *
     * @param node      nodo JSON del cual extraer el valor
     * @param fieldName nombre del campo a extraer
     * @return valor booleano del campo, o {@code null} si no existe o es nulo
     */
    private Boolean extractBooleanOrNull(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.path(fieldName).isNull()
                ? node.path(fieldName).asBoolean()
                : null;
    }

    /**
     * Construye la lista de mensajes que se envía al modelo, respetando el orden:
     * prompt del sistema → historial → mensaje actual del usuario.
     *
     * <p>El prompt del sistema se omite si es nulo o está en blanco.
     *
     * @param systemPrompt prompt del sistema que define el comportamiento del asistente
     * @param userMessage  mensaje actual enviado por el usuario
     * @param history      historial de mensajes previos de la conversación
     * @return lista de {@link Message} lista para enviarse al modelo
     */
    private List<Message> buildMessages(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        for (ConversationMessage cm : history) {
            switch (cm.role()) {
                case USER -> messages.add(new UserMessage(cm.content()));
                case ASSISTANT -> messages.add(new AssistantMessage(cm.content()));
            }
        }

        messages.add(new UserMessage(userMessage));
        return messages;
    }
}
