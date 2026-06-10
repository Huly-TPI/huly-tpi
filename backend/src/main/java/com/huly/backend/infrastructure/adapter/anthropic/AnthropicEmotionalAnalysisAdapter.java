package com.huly.backend.infrastructure.adapter.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
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
import java.util.Locale;
import java.util.Map;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicEmotionalAnalysisAdapter implements EmotionalAnalysisPort {

    private static final Map<String, EmotionType> EMOTION_ALIASES = Map.ofEntries(
            Map.entry("TRISTEZA", EmotionType.SADNESS),
            Map.entry("ALEGRIA", EmotionType.JOY),
            Map.entry("MIEDO", EmotionType.FEAR),
            Map.entry("ENOJO", EmotionType.ANGER),
            Map.entry("IRA", EmotionType.ANGER),
            Map.entry("ASCO", EmotionType.DISGUST),
            Map.entry("SORPRESA", EmotionType.SURPRISE),
            Map.entry("ANSIEDAD", EmotionType.ANXIETY),
            Map.entry("ESTRES", EmotionType.STRESS),
            Map.entry("ESTRÉS", EmotionType.STRESS),
            Map.entry("AGOBIO", EmotionType.OVERWHELM),
            Map.entry("PANICO", EmotionType.PANIC),
            Map.entry("PÁNICO", EmotionType.PANIC),
            Map.entry("DUELO", EmotionType.GRIEF),
            Map.entry("SOLEDAD", EmotionType.LONELINESS),
            Map.entry("CULPA", EmotionType.GUILT),
            Map.entry("VERGUENZA", EmotionType.SHAME),
            Map.entry("VERGÜENZA", EmotionType.SHAME),
            Map.entry("FRUSTRACION", EmotionType.FRUSTRATION),
            Map.entry("FRUSTRACIÓN", EmotionType.FRUSTRATION),
            Map.entry("CALMA", EmotionType.CALM),
            Map.entry("NEUTRAL", EmotionType.NEUTRAL)
    );

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicEmotionalAnalysisAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public EmotionalAnalysisResult analyze(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        try {
            String raw = extractText(chatModel.call(new Prompt(buildMessages(systemPrompt, userMessage, history))));
            return parseResponse(raw);
        } catch (Exception e) {
            log.warn("No se pudo analizar emocionalmente el mensaje, usando fallback neutral: {}", e.getMessage());
            return EmotionalAnalysisResult.neutral();
        }
    }

    private EmotionalAnalysisResult parseResponse(String raw) throws Exception {
        JsonNode node = objectMapper.readTree(extractJson(raw));
        return new EmotionalAnalysisResult(
                node.path("shouldRecommend").asBoolean(false),
                parseEmotion(node.path("detectedEmotion").asText(null)),
                clamp(node.path("confidence").asDouble(0.0), 0.0, 1.0),
                clamp(node.path("valence").asDouble(0.0), -1.0, 1.0),
                clamp(node.path("arousal").asDouble(0.0), -1.0, 1.0),
                clamp(node.path("dominance").asDouble(0.0), -1.0, 1.0),
                clamp(node.path("intensity").asDouble(0.0), 0.0, 1.0),
                textOrNull(node, "userGoal"),
                textOrNull(node, "shortReason")
        );
    }

    private EmotionType parseEmotion(String value) {
        if (value == null || value.isBlank()) {
            return EmotionType.NEUTRAL;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        EmotionType alias = EMOTION_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }

        try {
            return EmotionType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return EmotionType.NEUTRAL;
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String extractJson(String raw) {
        if (raw == null) {
            return "{}";
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private List<Message> buildMessages(String systemPrompt, String userMessage, List<ConversationMessage> history) {
        List<Message> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        List<ConversationMessage> safeHistory = history == null ? List.of() : history;
        for (ConversationMessage cm : safeHistory) {
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
