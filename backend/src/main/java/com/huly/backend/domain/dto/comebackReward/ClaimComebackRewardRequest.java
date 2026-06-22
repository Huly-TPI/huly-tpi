package com.huly.backend.domain.dto.comebackReward;

/**
 * Pedido para reclamar la recompensa por volver a la app.
 *
 * @param userId usuario que reclama la recompensa.
 */
public record ClaimComebackRewardRequest(Long userId) {
}
