package com.huly.backend.domain.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;

/**
 * Pedido de dominio para crear una palabra de riesgo.
 *
 * @param word        valor de la palabra de riesgo.
 * @param description descripcion opcional del contexto de riesgo.
 * @param severity    nivel de severidad de la palabra.
 */
public record CreateRiskWordRequest(String word, String description, RiskSeverity severity) {
}
