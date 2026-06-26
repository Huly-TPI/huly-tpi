package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para crear una meta de usuario.
 */
public record AddUserGoalRequest(
        Long userId,
        String title,
        String description,
        Long activityId
) {
}
