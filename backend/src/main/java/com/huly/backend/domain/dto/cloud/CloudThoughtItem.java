package com.huly.backend.domain.dto.cloud;

import java.time.Instant;

/**
 * Representacion de un pensamiento de nube dentro de la respuesta de dominio.
 */
public record CloudThoughtItem(
        Long id,
        String text,
        boolean workedOn,
        Instant createdAt
) {
}
