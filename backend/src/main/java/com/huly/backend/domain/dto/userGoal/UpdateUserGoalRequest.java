package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para actualizar una meta de usuario.
 */
public record UpdateUserGoalRequest(
        Long id,
        String title,
        String description,
        Long activityId
) {
}
