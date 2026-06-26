package com.huly.backend.domain.mapper.store;

import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.dto.store.InventoryItemView;
import com.huly.backend.domain.model.user.UserStoreItem;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de obtencion del inventario del usuario.
 */
public class GetUserInventoryMapper {

    public GetUserInventoryResponse toResponse(List<UserStoreItem> owned) {
        List<InventoryItemView> views = owned.stream()
                .map(this::toView)
                .toList();
        return new GetUserInventoryResponse(views);
    }

    private InventoryItemView toView(UserStoreItem owned) {
        return new InventoryItemView(
                owned.getStoreItem().getId(),
                owned.getStoreItem().getName(),
                owned.getStoreItem().getCategory(),
                owned.getStoreItem().getAssetKey(),
                owned.isEquipped()
        );
    }
}
