package com.huly.backend.domain.dto.badge;

import java.time.Instant;

/**
 * Representacion de una insignia dentro de la respuesta de dominio.
 */
public record BadgeItem(
        Long id,
        String code,
        String name,
        String description,
        String imageUrl,
        Instant createdAt
) {
}
