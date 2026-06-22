package com.huly.backend.domain.dto.comebackReward;

/**
 * Pedido para consultar el estado de la recompensa por volver a la app.
 *
 * @param userId usuario cuyo estado se consulta.
 */
public record GetComebackRewardStatusRequest(Long userId) {
}
