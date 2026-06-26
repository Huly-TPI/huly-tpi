package com.huly.backend.domain.mapper.store;

import com.huly.backend.domain.dto.store.UnequipStoreItemResponse;

/**
 * Mapper de dominio para el caso de uso de desequipar un item de la tienda.
 */
public class UnequipStoreItemMapper {

    public UnequipStoreItemResponse toResponse(boolean unequipped) {
        return new UnequipStoreItemResponse(unequipped);
    }
}
