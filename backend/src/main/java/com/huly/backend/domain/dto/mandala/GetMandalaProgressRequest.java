package com.huly.backend.domain.dto.mandala;

/**
 * Pedido de dominio para obtener el progreso de pintura de un mandala.
 *
 * @param userId    usuario propietario del progreso.
 * @param mandalaId identificador del mandala.
 */
public record GetMandalaProgressRequest(Long userId, String mandalaId) {
}
