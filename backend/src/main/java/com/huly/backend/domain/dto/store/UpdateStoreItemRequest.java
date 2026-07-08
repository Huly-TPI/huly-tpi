package com.huly.backend.domain.dto.store;

import com.huly.backend.domain.model.enums.ItemCategory;
import java.math.BigDecimal;

public record UpdateStoreItemRequest(
        Long id,
        String name,
        String description,
        ItemCategory category,
        int priceCoins,
        BigDecimal price,
        boolean premiumOnly,
        byte[] imageLight,
        byte[] imageDark,
        String imageContentType
) {
}