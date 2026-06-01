package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;

public record ChatReply(
        String content,
        EmotionType detectedEmotion,
        Integer intensity,
        Boolean riskDetected,
        String matchedWord,
        SuggestedChatAction suggestedAction
) {
    public ChatReply(
            String content,
            EmotionType detectedEmotion,
            Integer intensity,
            Boolean riskDetected,
            String matchedWord
    ) {
        this(content, detectedEmotion, intensity, riskDetected, matchedWord, null);
    }

    public static ChatReply of(String content) {
        return new ChatReply(content, null, null, null, null, null);
    }

    public ChatReply withSuggestedAction(SuggestedChatAction action) {
        return new ChatReply(content, detectedEmotion, intensity, riskDetected, matchedWord, action);
    }

    public ChatReply withEmotionalMetadata(EmotionType emotion, Integer normalizedIntensity) {
        return new ChatReply(content, emotion, normalizedIntensity, riskDetected, matchedWord, suggestedAction);
    }
}
