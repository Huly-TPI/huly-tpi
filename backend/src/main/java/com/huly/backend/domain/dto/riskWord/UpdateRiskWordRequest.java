package com.huly.backend.domain.dto.riskWord;

import com.huly.backend.domain.model.enums.RiskSeverity;

/**
 * Pedido de dominio para actualizar una palabra de riesgo existente.
 *
 * @param id          identificador de la palabra de riesgo a actualizar.
 * @param word        nuevo valor de la palabra.
 * @param description nueva descripcion (puede ser {@code null}).
 * @param severity    nuevo nivel de severidad.
 */
public record UpdateRiskWordRequest(Long id, String word, String description, RiskSeverity severity) {
}
