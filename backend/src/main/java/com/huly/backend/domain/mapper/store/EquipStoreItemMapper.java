package com.huly.backend.domain.mapper.store;

import com.huly.backend.domain.dto.store.EquipStoreItemResponse;

/**
 * Mapper de dominio para el caso de uso de equipar un item de la tienda.
 */
public class EquipStoreItemMapper {

    public EquipStoreItemResponse toResponse(boolean equipped) {
        return new EquipStoreItemResponse(equipped);
    }
}
