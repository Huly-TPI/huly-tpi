package com.huly.backend.domain.model.dailyReward;

/**
 * Progreso del ciclo a mostrar en el estado de recompensas diarias.
 *
 * @param nextDay       próximo día del ciclo a reclamar (0 si ya reclamó hoy o no hay config).
 * @param completedDays cantidad de días del ciclo ya completos.
 */
public record CycleProgress(int nextDay, int completedDays) {
}
