package com.huly.backend.domain.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;

/**
 * Respuesta de dominio luego de actualizar una palabra de riesgo.
 */
public record UpdateRiskWordResponse(
        Long id,
        String word,
        String description,
        RiskSeverity severity,
        boolean active
) {
}
