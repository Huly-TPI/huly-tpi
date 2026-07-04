package com.huly.backend.domain.mapper.store;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.model.shop.StoreItem;
public class StoreItemMapper {
    
    public StoreItemView toView(StoreItem item) {
        return new StoreItemView(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getAssetKey(),
                item.getPriceCoins(),
                item.getPrice(),
                item.isPremiumOnly(),
                item.getImageUrl()
        );
    }
}
