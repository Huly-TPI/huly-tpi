package com.huly.backend.domain.dto.comebackReward;

/**
 * Estado de la recompensa por volver a la app para un usuario.
 *
 * @param available    true si el usuario califica para reclamar la recompensa ahora.
 * @param daysInactive días transcurridos desde su última actividad registrada.
 * @param coins        monedas que otorgaría la recompensa (monto configurado).
 * @param thresholdDays días de inactividad requeridos para que la recompensa esté disponible.
 */
public record GetComebackRewardStatusResponse(
        boolean available,
        int daysInactive,
        int coins,
        int thresholdDays
) {
}
