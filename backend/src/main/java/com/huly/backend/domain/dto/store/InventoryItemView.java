package com.huly.backend.domain.dto.store;

import com.huly.backend.domain.model.enums.ItemCategory;

/**
 * Representacion de un item del inventario del usuario dentro de la respuesta de dominio.
 */
public record InventoryItemView(
        Long storeItemId,
        String name,
        ItemCategory category,
        String assetKey,
        boolean equipped
) {
}
