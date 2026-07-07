package com.huly.backend.domain.dto.chat;

import com.huly.backend.domain.model.enums.ActivityType;

/**
 * Acción sugerida como DTO de dominio, desacoplada del value object del model.
 */
public record SuggestedActionResponse(
        ActivityType type,
        Long activityId,
        String title,
        String description,
        String actionUrl,
        Long emotionalEventId
) {
}
