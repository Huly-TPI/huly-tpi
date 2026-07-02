package com.huly.backend.domain.dto.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;

import java.time.Instant;
import java.util.List;

/**
 * Response de dominio del historial de chat: los mensajes de la página más los metadatos de
 * paginación, sin exponer {@code Page} ni el modelo {@code ChatMessage}.
 */
public record ChatHistoryResponse(
        List<Message> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public record Message(
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
    ) {
    }
}
