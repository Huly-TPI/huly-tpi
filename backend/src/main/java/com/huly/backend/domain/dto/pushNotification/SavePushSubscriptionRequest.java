package com.huly.backend.domain.dto.pushNotification;

/**
 * Pedido de dominio para guardar una suscripcion push.
 *
 * @param userId   usuario al que pertenece la suscripcion.
 * @param endpoint endpoint de la suscripcion push.
 * @param p256dh   clave publica p256dh de la suscripcion.
 * @param auth     secreto de autenticacion de la suscripcion.
 */
public record SavePushSubscriptionRequest(Long userId, String endpoint, String p256dh, String auth) {
}
