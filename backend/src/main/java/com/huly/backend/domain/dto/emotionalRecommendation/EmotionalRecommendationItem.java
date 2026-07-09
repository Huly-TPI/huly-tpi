package com.huly.backend.domain.dto.emotionalRecommendation;

import com.huly.backend.domain.model.enums.ActivityType;

/**
 * Representacion de una recomendacion dentro de la respuesta de dominio.
 */
public record EmotionalRecommendationItem(
        Long activityId,
        ActivityType type,
        String title,
        String description,
        double score,
        String reason,
        String routePath
) {
}
