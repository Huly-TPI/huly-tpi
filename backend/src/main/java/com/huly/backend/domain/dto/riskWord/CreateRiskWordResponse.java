package com.huly.backend.domain.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;

/**
 * Respuesta de dominio luego de crear una palabra de riesgo.
 */
public record CreateRiskWordResponse(
        Long id,
        String word,
        String description,
        RiskSeverity severity,
        boolean active
) {
}
