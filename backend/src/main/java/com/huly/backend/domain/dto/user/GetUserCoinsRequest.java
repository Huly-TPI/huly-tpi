package com.huly.backend.domain.dto.user;

/**
 * Pedido de dominio para obtener los coins de un usuario.
 *
 * @param userId usuario del que se consultan los coins.
 */
public record GetUserCoinsRequest(Long userId) {
}
