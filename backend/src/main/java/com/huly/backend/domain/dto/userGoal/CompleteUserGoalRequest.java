package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para completar una meta de usuario.
 * La imagen asociada se pasa como parametro separado (tipo binario de infraestructura).
 */
public record CompleteUserGoalRequest(Long id) {
}
