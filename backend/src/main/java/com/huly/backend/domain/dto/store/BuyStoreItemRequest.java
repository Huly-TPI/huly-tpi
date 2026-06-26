package com.huly.backend.domain.dto.store;

/**
 * Pedido de dominio para comprar un item de la tienda.
 *
 * @param userId      usuario que compra el item.
 * @param storeItemId item a comprar.
 */
public record BuyStoreItemRequest(Long userId, Long storeItemId) {
}
