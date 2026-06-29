package com.huly.backend.domain.model.dailyReward;

import java.time.LocalDate;
import java.util.List;

/**
 * Reglas del ciclo de recompensas diarias.
 *
 * La racha ({@code streak}) es un total acumulado que crece mientras el usuario
 * reclama días consecutivos y solo se reinicia a 1 si se saltea un día. El día del
 * ciclo (1..N) que determina las monedas se deriva de la racha con {@link #cycleDay},
 * de modo que al completar el ciclo los premios vuelven al Día 1 pero el número de
 * racha sigue creciendo.
 */
public final class DailyRewardCycle {

    private DailyRewardCycle() {
    }

    /** Multiplicador de monedas para usuarios con un plan/membresía activa. */
    public static final double PLAN_BONUS_MULTIPLIER = 1.5;

    /** Aplica el bonus por plan al monto base, redondeando al entero más cercano. */
    public static int applyPlanBonus(int baseCoins, boolean hasPlan) {
        return hasPlan ? (int) Math.round(baseCoins * PLAN_BONUS_MULTIPLIER) : baseCoins;
    }

    /** True si el último reclamo fue hoy o ayer (la racha sigue viva). */
    public static boolean isAlive(DailyClaimState state, LocalDate today) {
        LocalDate last = state.lastClaimDate();
        return last != null && (last.equals(today) || last.equals(today.minusDays(1)));
    }

    /** Racha vigente a mostrar: la racha del estado si sigue viva, o 0 si se rompió. */
    public static int currentStreak(DailyClaimState state, LocalDate today) {
        return isAlive(state, today) ? state.streak() : 0;
    }

    /**
     * Monedas que corresponden a un día del ciclo (1..N) según la config.
     * Si la config no tuviera ese día (N variable / huecos) cae al primer día.
     * Precondición: {@code cycle} no vacío.
     */
    public static int coinsForDay(List<DailyReward> cycle, int cycleDay) {
        return cycle.stream()
                .filter(r -> r.getDayNumber() == cycleDay)
                .findFirst()
                .map(DailyReward::getCoins)
                .orElseGet(() -> cycle.get(0).getCoins());
    }

    /**
     * Progreso del ciclo dado el estado, la fecha de hoy, el largo del ciclo y si aún puede
     * reclamar hoy. Si puede reclamar, el próximo día es el que reclamaría y los completos son
     * los anteriores; si ya reclamó hoy, no hay próximo día y los completos son hasta su racha.
     * Si no hay ciclo configurado devuelve cero/cero.
     */
    public static CycleProgress progress(DailyClaimState state, LocalDate today,
                                         int cycleLength, boolean canClaimToday) {
        if (cycleLength <= 0) {
            return new CycleProgress(0, 0);
        }
        if (canClaimToday) {
            int nextDay = cycleDay(nextStreak(state, today), cycleLength);
            return new CycleProgress(nextDay, nextDay - 1);
        }
        return new CycleProgress(0, cycleDay(state.streak(), cycleLength));
    }

    /**
     * Racha total que tendría el usuario si reclama hoy: +1 si viene de ayer
     * (consecutivo), o 1 si se salteó un día o es su primer reclamo.
     */
    public static int nextStreak(DailyClaimState state, LocalDate today) {
        boolean consecutive = state.lastClaimDate() != null
                && state.lastClaimDate().equals(today.minusDays(1));
        return consecutive ? state.streak() + 1 : 1;
    }

    /**
     * Día del ciclo (1..cycleLength) correspondiente a una racha total dada.
     * Devuelve 0 si la racha es 0 (nunca reclamó).
     */
    public static int cycleDay(int streak, int cycleLength) {
        if (streak <= 0) {
            return 0;
        }
        return ((streak - 1) % cycleLength) + 1;
    }
}
