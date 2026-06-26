package com.huly.backend.domain.dto.userPlant;

import java.time.Instant;

/**
 * Representacion de una planta de usuario dentro de las respuestas de dominio.
 */
public record UserPlantItem(
        Long id,
        Integer plantNumber,
        Integer requiredGoals,
        Long completedGoalsCount,
        String status,
        Instant startedAt,
        Instant completedAt
) {
}
