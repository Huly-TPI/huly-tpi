package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.provider.StreamingLLMChatPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador que conecta el dominio con la API de Anthropic (Claude) via Spring AI.
 * Implementa tanto la interfaz bloqueante (LLMChatPort) como la de streaming (StreamingLLMChatPort).
 *
 * @Primary: es el adaptador activo cuando hay múltiples implementaciones (ej: NoOp para tests).
 * @ConditionalOnProperty: solo se registra como bean si app.ai.provider=anthropic en properties.
 *
 * Parseo de respuestas:
 * La IA responde en JSON estructurado con campos como huly_reply, detected_emotion, intensity, etc.
 * Si el parseo falla (la IA devuelve texto libre), se usa el raw como contenido y se omite la metadata.
 * extractJson() limpia texto extra que la IA puede agregar antes o después del JSON.
 */
@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicChatAdapter implements LLMChatPort, StreamingLLMChatPort {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicChatAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Llamada bloqueante a la IA: construye el prompt completo y espera la respuesta completa.
     * La respuesta se parsea como JSON estructurado para extraer emoción, riesgo y contenido.
     */
    @Override
    public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = buildMessages(systemPrompt, userMessage, history);
        String raw = extractText(chatModel.call(new Prompt(messages)));
        return parseResponse(raw);
    }

    /**
     * Llamada en streaming: devuelve un Flux que emite cada fragmento de texto conforme la IA lo genera.
     * El stream no incluye metadata emocional (JSON no es válido fragmentado).
     * Los fragmentos vacíos se filtran para no enviar SSE innecesarios al cliente.
     */
    @Override
    public Flux<String> stream(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = buildMessages(systemPrompt, userMessage, history);
        return chatModel.stream(new Prompt(messages))
                .map(this::extractText)
                .filter(text -> text != null && !text.isBlank());
    }

    /**
     * Parsea la respuesta JSON estructurada de la IA.
     * Si falla, devuelve el texto crudo como contenido sin metadata (degradación graceful).
     */
    private ChatReply parseResponse(String raw) {
        try {
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);

            // huly_reply es la respuesta conversacional; fallback al raw si no está
            String reply = node.path("huly_reply").asText(raw);
            EmotionType emotion = parseEmotion(node.path("detected_emotion").asText(null));
            Integer intensity = extractIntOrNull(node, "intensity");
            Boolean riskDetected = extractBooleanOrNull(node, "risk_detected");
            String matchedWord = node.path("matched_word").isNull()
                    ? null : node.path("matched_word").asText(null);
            ChatReply.GeneratedChallenge generatedChallenge = parseGeneratedChallenge(node);

            return new ChatReply(reply, emotion, intensity, riskDetected, matchedWord, null, generatedChallenge);
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta estructurada, usando texto plano");
            return ChatReply.of(raw); // Fallback: devuelve el texto tal cual sin metadata
        }
    }

    /** Extrae el reto generado del JSON si existe y tiene título válido. */
    private ChatReply.GeneratedChallenge parseGeneratedChallenge(JsonNode node) {
        JsonNode challengeNode = node.path("generated_challenge");
        if (challengeNode.isNull() || challengeNode.isMissingNode() || !challengeNode.isObject()) {
            return null;
        }
        String title = challengeNode.path("title").asText(null);
        String description = challengeNode.path("description").asText(null);
        if (title == null || title.isBlank()) {
            return null;
        }
        return new ChatReply.GeneratedChallenge(title, description);
    }

    /** Extrae el texto de la respuesta de Spring AI, con null-safety en toda la cadena. */
    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    /** Extrae el bloque JSON más externo del texto (la IA puede agregar prose antes/después). */
    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw; // Si no hay llaves, retorna el raw y que falle el parseo del siguiente paso
    }

    /** Convierte el string de emoción al enum EmotionType (case-insensitive). Null si inválido. */
    private EmotionType parseEmotion(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return EmotionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer extractIntOrNull(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.path(fieldName).isNull()
                ? node.path(fieldName).asInt()
                : null;
    }

    private Boolean extractBooleanOrNull(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.path(fieldName).isNull()
                ? node.path(fieldName).asBoolean()
                : null;
    }

    /**
     * Construye la lista de mensajes para la IA en el orden correcto:
     *  1. SystemMessage (instrucciones de Huly)
     *  2. Historial de la conversación (USER / ASSISTANT alternados)
     *  3. Nuevo mensaje del usuario
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
