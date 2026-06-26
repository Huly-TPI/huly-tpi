package com.huly.backend.domain.dto.mandala;

/**
 * Representacion de un mandala disponible dentro de la respuesta de dominio.
 */
public record MandalaItem(
        String id,
        String title,
        String description,
        String assetKey,
        int displayOrder,
        String unlockSource,
        String accessType,
        boolean locked
) {
}
