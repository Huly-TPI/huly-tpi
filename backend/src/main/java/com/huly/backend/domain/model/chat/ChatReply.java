package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;

public record ChatReply(
        String content,
        EmotionType detectedEmotion,
        Integer intensity,
        Boolean riskDetected,
        String matchedWord,
        SuggestedChatAction suggestedAction,
        GeneratedChallenge generatedChallenge
) {
    public record GeneratedChallenge(String title, String description) {

        public static GeneratedChallenge defaultActionChallenge() {
            return new GeneratedChallenge(
                    "Reto de accion pequena",
                    "Elegí una acción simple que puedas hacer en los próximos 10 minutos "
                            + "y realizala sin buscar que salga perfecta."
            );
        }
    }

    public ChatReply(
            String content,
            EmotionType detectedEmotion,
            Integer intensity,
            Boolean riskDetected,
            String matchedWord
    ) {
        this(content, detectedEmotion, intensity, riskDetected, matchedWord, null, null);
    }

    public ChatReply(
            String content,
            EmotionType detectedEmotion,
            Integer intensity,
            Boolean riskDetected,
            String matchedWord,
            SuggestedChatAction suggestedAction
    ) {
        this(content, detectedEmotion, intensity, riskDetected, matchedWord, suggestedAction, null);
    }

    public static ChatReply of(String content) {
        return new ChatReply(content, null, null, null, null, null, null);
    }

    public ChatReply withSuggestedAction(SuggestedChatAction action) {
        return new ChatReply(content, detectedEmotion, intensity, riskDetected, matchedWord, action, generatedChallenge);
    }

    public ChatReply withEmotionalMetadata(EmotionType emotion, Integer normalizedIntensity) {
        return new ChatReply(content, emotion, normalizedIntensity, riskDetected, matchedWord, suggestedAction, generatedChallenge);
    }

    public ChatReply withContent(String newContent) {
        return new ChatReply(newContent, detectedEmotion, intensity, riskDetected, matchedWord, suggestedAction, generatedChallenge);
    }

    public ChatReply withGeneratedChallenge(GeneratedChallenge challenge) {
        return new ChatReply(content, detectedEmotion, intensity, riskDetected, matchedWord, suggestedAction, challenge);
    }

    public ChatReply withoutGeneratedChallenge() {
        return withGeneratedChallenge(null);
    }

    /**
     * Devuelve una copia con {@code extra} agregado al final del contenido, separado por una
     * línea en blanco (o como contenido único si estaba vacío).
     */
    public ChatReply appendContent(String extra) {
        String next = content == null || content.isBlank()
                ? extra
                : content.trim() + "\n\n" + extra;
        return withContent(next);
    }

    /**
     * Indica si es seguro ofrecer la pregunta de estilo de comunicación: sin riesgo detectado,
     * con intensidad emocional baja y sin una acción sugerida en curso.
     */
    public boolean canOfferCommunicationStyle() {
        return !Boolean.TRUE.equals(riskDetected)
                && (intensity == null || intensity < 7)
                && suggestedAction == null;
    }
}
