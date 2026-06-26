package com.huly.backend.domain.dto.riskWord;

/**
 * Pedido de dominio para eliminar una palabra de riesgo.
 *
 * @param id identificador de la palabra de riesgo a eliminar.
 */
public record DeleteRiskWordRequest(Long id) {
}
