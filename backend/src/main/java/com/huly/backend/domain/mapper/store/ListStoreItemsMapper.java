package com.huly.backend.domain.mapper.store;

import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.model.shop.StoreItem;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de items de la tienda.
 */
public class ListStoreItemsMapper {

    public ListStoreItemsResponse toResponse(List<StoreItem> items) {
        List<StoreItemView> views = items.stream()
                .map(this::toView)
                .toList();
        return new ListStoreItemsResponse(views);
    }

    private StoreItemView toView(StoreItem item) {
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
