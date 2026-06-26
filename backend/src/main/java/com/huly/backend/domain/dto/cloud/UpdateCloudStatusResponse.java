package com.huly.backend.domain.dto.cloud;

import com.huly.backend.domain.model.enums.CloudStatus;

import java.time.Instant;

/**
 * Respuesta de dominio luego de actualizar el estado de un pensamiento de nube.
 */
public record UpdateCloudStatusResponse(
        Long id,
        String text,
        CloudStatus status,
        boolean workedOn,
        Instant createdAt
) {
}
