package com.huly.backend.domain.dto.store;

import com.huly.backend.domain.model.enums.ItemCategory;

import java.math.BigDecimal;

/**
 * Representacion de un item de la tienda dentro de la respuesta de dominio.
 */
public record StoreItemView(
        Long id,
        String name,
        String description,
        ItemCategory category,
        String assetKey,
        int priceCoins,
        BigDecimal price,
        boolean premiumOnly,
        String imageUrl
) {
}
