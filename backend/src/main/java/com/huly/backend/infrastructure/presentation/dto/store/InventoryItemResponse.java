package com.huly.backend.infrastructure.presentation.dto.store;

public record InventoryItemResponse(
        Long storeItemId,
        String name,
        String category,
        String assetKey,
        boolean equipped
) {
}