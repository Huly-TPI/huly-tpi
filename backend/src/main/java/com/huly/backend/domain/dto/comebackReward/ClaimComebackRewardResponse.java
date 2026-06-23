package com.huly.backend.domain.dto.comebackReward;

/**
 * Resultado de reclamar la recompensa por volver a la app.
 *
 * @param granted      true si se otorgó la recompensa (el usuario calificaba).
 * @param coins        monedas acreditadas (0 si no se otorgó).
 * @param daysInactive días de inactividad detectados al momento del reclamo.
 */
public record ClaimComebackRewardResponse(boolean granted, int coins, int daysInactive) {
}
