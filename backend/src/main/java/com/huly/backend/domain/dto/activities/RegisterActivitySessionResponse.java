package com.huly.backend.domain.dto.activities;

import com.huly.backend.domain.model.enums.ActivityType;

import java.time.Instant;

/**
 * Respuesta de dominio luego de registrar una sesion de actividad.
 */
public record RegisterActivitySessionResponse(
        Long id,
        Long userId,
        ActivityType activityType,
        Instant createdAt
) {
}
