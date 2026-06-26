package com.huly.backend.domain.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;

/**
 * Representacion de una palabra de riesgo dentro de la respuesta de dominio.
 */
public record RiskWordItem(
        Long id,
        String word,
        String description,
        RiskSeverity severity,
        boolean active
) {
}
