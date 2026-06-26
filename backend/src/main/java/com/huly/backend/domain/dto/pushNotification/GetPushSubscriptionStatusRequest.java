package com.huly.backend.domain.dto.pushNotification;

/**
 * Pedido de dominio para consultar el estado de suscripcion push de un usuario.
 *
 * @param userId usuario del que se consulta el estado.
 */
public record GetPushSubscriptionStatusRequest(Long userId) {
}
