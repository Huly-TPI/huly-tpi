package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;

public record ConversationMessage(
        MessageRole role,
        String content,
        EmotionType detectedEmotion,
        Boolean riskDetected,
        String matchedWord,
        SuggestedChatAction suggestedAction,
        ChatReply.GeneratedChallenge generatedChallenge,
        String suggestedActionDecision,
        String challengeDecision
) {
    public static ConversationMessage of(MessageRole role, String content) {
        return new ConversationMessage(role, content, null, null, null, null, null, null, null);
    }
}
