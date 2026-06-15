package com.huly.backend.domain.model;
import com.huly.backend.domain.model.enums.ItemCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreItem {
    private final Long id;
    private final String name;
    private final String description;
    private final ItemCategory category;
    private final String assetKey;
    private final int priceCoins;
    
}
