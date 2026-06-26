package com.huly.backend.domain.dto.activities;

import com.huly.backend.domain.model.enums.ActivityType;

/**
 * Representacion de una actividad dentro de la respuesta de dominio.
 */
public record ActivityItem(
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
