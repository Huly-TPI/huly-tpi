package com.huly.backend.domain.dto.mandala;

/**
 * Pedido de dominio para guardar el progreso de pintura de un mandala.
 *
 * @param userId    usuario propietario del progreso.
 * @param mandalaId identificador del mandala.
 * @param paintBlob contenido binario del progreso de pintura.
 */
public record SaveMandalaProgressRequest(Long userId, String mandalaId, byte[] paintBlob) {
}
