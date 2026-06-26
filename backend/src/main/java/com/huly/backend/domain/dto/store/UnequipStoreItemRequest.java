package com.huly.backend.domain.dto.store;

/**
 * Pedido de dominio para desequipar un item de la tienda.
 *
 * @param userId      usuario que desequipa el item.
 * @param storeItemId item a desequipar.
 */
public record UnequipStoreItemRequest(Long userId, Long storeItemId) {
}
