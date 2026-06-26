package com.huly.backend.domain.dto.pushNotification;

/**
 * Respuesta de dominio con el estado de suscripcion push de un usuario.
 */
public record GetPushSubscriptionStatusResponse(boolean subscribed) {
}
