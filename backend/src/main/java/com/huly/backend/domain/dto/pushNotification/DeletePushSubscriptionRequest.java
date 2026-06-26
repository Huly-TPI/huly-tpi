package com.huly.backend.domain.dto.pushNotification;

/**
 * Pedido de dominio para eliminar una suscripcion push.
 *
 * @param endpoint endpoint de la suscripcion a eliminar.
 */
public record DeletePushSubscriptionRequest(String endpoint) {
}
