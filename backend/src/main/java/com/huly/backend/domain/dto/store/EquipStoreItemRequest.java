package com.huly.backend.domain.dto.store;

/**
 * Pedido de dominio para equipar un item de la tienda.
 *
 * @param userId      usuario que equipa el item.
 * @param storeItemId item a equipar.
 */
public record EquipStoreItemRequest(Long userId, Long storeItemId) {
}
