package com.huly.backend.domain.dto.mandala;

/**
 * Pedido de dominio para eliminar el progreso de pintura de un mandala.
 *
 * @param userId    usuario propietario del progreso.
 * @param mandalaId identificador del mandala.
 */
public record ClearMandalaProgressRequest(Long userId, String mandalaId) {
}
