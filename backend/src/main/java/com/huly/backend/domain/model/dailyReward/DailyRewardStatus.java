package com.huly.backend.domain.model.dailyReward;

import java.util.List;

/**
 * Estado del calendario de recompensas diarias para un usuario.
 *
 * @param days          configuración del ciclo (Día 1..N con sus monedas).
 * @param currentStreak racha actual del usuario (último día reclamado; 0 = nunca).
 * @param canClaimToday true si todavía no reclamó hoy.
 * @param nextDay       día del ciclo que reclamaría si reclama ahora (1..N).
 */
public record DailyRewardStatus(
        List<DailyReward> days,
        int currentStreak,
        boolean canClaimToday,
        int nextDay
) {
}
