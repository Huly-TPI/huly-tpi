package com.huly.backend.domain.dto.dailyReward;

/**
 * Pedido para reclamar la recompensa diaria.
 *
 * @param userId usuario que reclama la recompensa.
 */
public record ClaimDailyRewardRequest(Long userId) {
}
