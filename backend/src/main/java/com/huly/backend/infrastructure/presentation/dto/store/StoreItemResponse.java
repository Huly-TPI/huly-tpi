package com.huly.backend.infrastructure.presentation.dto.store;

import java.math.BigDecimal;

public record StoreItemResponse(
        Long id,
        String name,
        String description,
        String category,
        String assetKey,
        int priceCoins, 
        BigDecimal price,
        boolean premiumOnly, 
        String imageUrlLight, 
        String imageUrlDark
) {
}