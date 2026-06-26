package com.huly.backend.domain.dto.cloud;

import java.time.Instant;

/**
 * Respuesta de dominio luego de crear un pensamiento de nube.
 */
public record CreateCloudThoughtResponse(
        Long id,
        String text,
        boolean workedOn,
        Instant createdAt
) {
}
