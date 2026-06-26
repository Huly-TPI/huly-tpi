package com.huly.backend.domain.dto.pushNotification;

/**
 * Respuesta de dominio luego de procesar la baja de emails de re-engagement.
 */
public record UnsubscribeFromEmailsResponse(boolean success) {
}
