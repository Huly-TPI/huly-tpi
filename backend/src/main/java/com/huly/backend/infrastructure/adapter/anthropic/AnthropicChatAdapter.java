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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public ChatReply chat(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = buildMessages(systemPrompt, userMessage, history);
        String raw = extractText(chatModel.call(new Prompt(messages)));
        return parseResponse(raw);
    }

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
            ChatReply.GeneratedChallenge generatedChallenge = parseGeneratedChallenge(node);

            return new ChatReply(reply, emotion, intensity, riskDetected, matchedWord, null, generatedChallenge);
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta estructurada, usando texto plano");
            return ChatReply.of(raw);
        }
    }

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

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

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

        messages.add(new UserMessage(userMessage
                + "\n\n[FORMATO OBLIGATORIO: Tu respuesta debe ser ÚNICAMENTE el objeto JSON válido. "
                + "Sin texto introductorio, sin explicaciones, sin markdown. "
                + "Comienza directamente con { y termina con }.]"));
        return messages;
    }
}
