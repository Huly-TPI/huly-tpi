package com.huly.backend.domain.dto.userGoal;

/**
 * Pedido de dominio para cancelar (eliminar logicamente) una meta de usuario.
 */
public record DeleteUserGoalRequest(Long id) {
}
