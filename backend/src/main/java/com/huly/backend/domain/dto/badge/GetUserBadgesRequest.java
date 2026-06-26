package com.huly.backend.domain.dto.badge;

/**
 * Pedido de dominio para obtener las insignias de un usuario.
 *
 * @param userId usuario del que se quieren obtener las insignias.
 */
public record GetUserBadgesRequest(Long userId) {
}
