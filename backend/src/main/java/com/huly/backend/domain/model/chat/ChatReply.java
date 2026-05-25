package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;

public record ChatReply(
        String content,
        EmotionType detectedEmotion,
        Integer intensity,
        Boolean riskDetected,
        String matchedWord
) {
    public static ChatReply of(String content) {
        return new ChatReply(content, null, null, null, null);
    }
}
