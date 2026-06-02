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
    public record GeneratedChallenge(String title, String description) {}

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
}
