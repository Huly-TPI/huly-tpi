package com.huly.backend.domain.model.dailyReward;

import java.util.List;

/**
 * Estado del calendario de recompensas diarias para un usuario.
 *
 * @param days          configuración del ciclo (Día 1..N con sus monedas).
 * @param currentStreak racha total viva del usuario (0 si la rompió o nunca reclamó).
 * @param completedDays días del ciclo ya completados en la pasada actual (0..N).
 * @param canClaimToday true si todavía no reclamó hoy.
 * @param nextDay       día del ciclo (1..N) que reclamaría hoy; 0 si ya reclamó hoy.
 */
public record DailyRewardStatus(
        List<DailyReward> days,
        int currentStreak,
        int completedDays,
        boolean canClaimToday,
        int nextDay
) {
}
