package com.huly.backend.domain.dto.chat;

import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;

import java.time.Instant;
import java.util.List;

/**
 * Response de dominio del historial de chat: los mensajes de la página más los metadatos de
 * paginación, con DTOs propios (sin exponer {@code Page} ni tipos del model).
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
            SuggestedActionResponse suggestedAction,
            GeneratedChallengeResponse generatedChallenge,
            String suggestedActionDecision,
            String challengeDecision
    ) {
    }
}
