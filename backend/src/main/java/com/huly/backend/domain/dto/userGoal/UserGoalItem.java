package com.huly.backend.domain.dto.userGoal;

import java.time.Instant;

/**
 * Representacion de una meta de usuario dentro de las respuestas de dominio.
 */
public record UserGoalItem(
        Long id,
        Long userId,
        String title,
        String description,
        String status,
        Instant createdAt,
        Long activityId,
        String imageUrl,
        Integer coinsReward,
        Integer coinsRewardWithImage
) {
}
