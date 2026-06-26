package com.huly.backend.domain.dto.userGoal;

/**
 * Respuesta de dominio con las metas completadas y pendientes de un usuario.
 */
public record GetUserGoalsResponse(
        UserGoalPage completados,
        UserGoalPage pendientes
) {
}
