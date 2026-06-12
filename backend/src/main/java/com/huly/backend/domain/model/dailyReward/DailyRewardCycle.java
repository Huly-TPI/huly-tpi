package com.huly.backend.domain.model.dailyReward;

import java.time.LocalDate;

/**
 * Reglas del ciclo de recompensas diarias.
 *
 * Ciclo infinito de {@code cycleLength} días: tras reclamar el último día,
 * el próximo reclamo consecutivo vuelve al Día 1. Si el usuario se saltea un día
 * (o es su primer reclamo), la racha se reinicia al Día 1.
 */
public final class DailyRewardCycle {

    private DailyRewardCycle() {
    }

    /**
     * Calcula qué día del ciclo (1..cycleLength) correspondería reclamar hoy.
     *
     * @param state       estado actual de la racha del usuario.
     * @param today       fecha de hoy (en la zona horaria de negocio).
     * @param cycleLength cantidad de días del ciclo (N).
     */
    public static int computeNextDay(DailyClaimState state, LocalDate today, int cycleLength) {
        boolean consecutive = state.lastClaimDate() != null
                && state.lastClaimDate().equals(today.minusDays(1));

        if (consecutive && state.streak() >= 1 && state.streak() < cycleLength) {
            return state.streak() + 1;
        }
        // Primer reclamo, día salteado, o ciclo completado -> reinicia a Día 1.
        return 1;
    }
}
