package com.huly.backend.domain.dto.pushNotification;

/**
 * Respuesta de dominio luego de eliminar una suscripcion push.
 */
public record DeletePushSubscriptionResponse(boolean deleted) {
}
