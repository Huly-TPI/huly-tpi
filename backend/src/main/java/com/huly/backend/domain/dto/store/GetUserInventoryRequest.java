package com.huly.backend.domain.dto.store;

/**
 * Pedido de dominio para obtener el inventario de un usuario.
 *
 * @param userId usuario del que se solicita el inventario.
 */
public record GetUserInventoryRequest(Long userId) {
}
