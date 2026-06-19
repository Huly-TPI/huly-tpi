package com.huly.backend.infrastructure.presentation.dto.store;

public record StoreItemResponse(
        Long id,
        String name,
        String description,
        String category,
        String assetKey,
        int priceCoins
) {
}