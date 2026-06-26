package com.huly.backend.infrastructure.presentation.mapper.store;

import com.huly.backend.domain.dto.store.BuyStoreItemRequest;
import com.huly.backend.domain.dto.store.EquipStoreItemRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.dto.store.InventoryItemView;
import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UnequipStoreItemRequest;
import com.huly.backend.infrastructure.presentation.dto.store.InventoryItemResponse;
import com.huly.backend.infrastructure.presentation.dto.store.StoreItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de tienda:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class StorePresentationMapper {

    public GetUserInventoryRequest toInventoryRequest(Long userId) {
        return new GetUserInventoryRequest(userId);
    }

    public BuyStoreItemRequest toBuyRequest(Long userId, Long storeItemId) {
        return new BuyStoreItemRequest(userId, storeItemId);
    }

    public EquipStoreItemRequest toEquipRequest(Long userId, Long storeItemId) {
        return new EquipStoreItemRequest(userId, storeItemId);
    }

    public UnequipStoreItemRequest toUnequipRequest(Long userId, Long storeItemId) {
        return new UnequipStoreItemRequest(userId, storeItemId);
    }

    public List<StoreItemResponse> toStoreItemResponses(ListStoreItemsResponse response) {
        return response.items().stream()
                .map(this::toStoreItemResponse)
                .toList();
    }

    public List<InventoryItemResponse> toInventoryResponses(GetUserInventoryResponse response) {
        return response.items().stream()
                .map(this::toInventoryResponse)
                .toList();
    }

    private StoreItemResponse toStoreItemResponse(StoreItemView item) {
        return new StoreItemResponse(
                item.id(),
                item.name(),
                item.description(),
                item.category().name(),
                item.assetKey(),
                item.priceCoins(),
                item.price(),
                item.premiumOnly()
        );
    }

    private InventoryItemResponse toInventoryResponse(InventoryItemView item) {
        return new InventoryItemResponse(
                item.storeItemId(),
                item.name(),
                item.category().name(),
                item.assetKey(),
                item.equipped()
        );
    }
}
