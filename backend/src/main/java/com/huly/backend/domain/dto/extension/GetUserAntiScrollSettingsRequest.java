package com.huly.backend.domain.dto.extension;

/**
 * Pedido de dominio para obtener la configuracion anti-scroll de un usuario.
 *
 * @param userId usuario del que se obtiene la configuracion.
 */
public record GetUserAntiScrollSettingsRequest(Long userId) {
}
