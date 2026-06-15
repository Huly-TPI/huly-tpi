package com.huly.backend.domain.model.dailyReward;

/**
 * Resultado de reclamar la recompensa diaria.
 *
 * @param coins      monedas acreditadas en este reclamo.
 * @param dayNumber  día del ciclo que se reclamó (1..N).
 * @param newStreak  nueva racha del usuario tras el reclamo (== dayNumber).
 */
public record DailyRewardClaim(int coins, int dayNumber, int newStreak) {
}
