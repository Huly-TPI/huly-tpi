package com.huly.backend.infrastructure.presentation.dto.activities;

import com.huly.backend.domain.model.enums.ActivityType;

/**
 * DTO web con los datos de una actividad expuesta por la API.
 */
public record ActivityResponse(
        Long id,
        ActivityType type,
        double valenceMin,
        double valenceMax,
        double arousalMin,
        double arousalMax,
        double dominanceMin,
        double dominanceMax,
        double effectValence,
        double effectArousal,
        double effectDominance
) {
}
