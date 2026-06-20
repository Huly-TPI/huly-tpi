package com.huly.backend.domain.dto.dailyReward;

/**
 * Pedido para consultar el estado del calendario de recompensas diarias.
 *
 * @param userId usuario cuyo estado se consulta.
 */
public record GetDailyRewardStatusRequest(Long userId) {
}
