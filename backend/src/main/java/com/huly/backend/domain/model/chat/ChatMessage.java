package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;

import java.time.Instant;

public record ChatMessage(
        Long id,
        MessageRole role,
        String content,
        Boolean riskDetected,
        EmotionType detectedEmotion,
        Instant createdAt,
        SuggestedChatAction suggestedAction,
        ChatReply.GeneratedChallenge generatedChallenge,
        String suggestedActionDecision,
        String challengeDecision
) {}
