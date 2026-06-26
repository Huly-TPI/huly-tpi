package com.huly.backend.domain.dto.user;

/**
 * Pedido de dominio para obtener la membresia vigente de un usuario.
 *
 * @param userId usuario del que se consulta la membresia.
 */
public record GetCurrentMembershipRequest(Long userId) {
}
