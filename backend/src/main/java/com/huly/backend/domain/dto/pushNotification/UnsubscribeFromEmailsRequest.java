package com.huly.backend.domain.dto.pushNotification;

/**
 * Pedido de dominio para dar de baja los emails de re-engagement de un usuario.
 *
 * @param token token de baja recibido en el email.
 */
public record UnsubscribeFromEmailsRequest(String token) {
}
