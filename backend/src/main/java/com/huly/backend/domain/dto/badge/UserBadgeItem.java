package com.huly.backend.domain.dto.badge;

import java.time.Instant;

/**
 * Representacion de una insignia obtenida por el usuario dentro de la respuesta de dominio.
 */
public record UserBadgeItem(
        Long id,
        BadgeItem badge,
        Instant obtainedAt
) {
}
