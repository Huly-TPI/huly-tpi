package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para aceptar un reto y crear la meta asociada.
 */
public record AcceptChallengeRequest(
        Long userId,
        String title,
        String description,
        Long activityId
) {
}
