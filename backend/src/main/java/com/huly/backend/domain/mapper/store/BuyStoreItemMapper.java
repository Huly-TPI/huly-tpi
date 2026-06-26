package com.huly.backend.domain.mapper.store;

import com.huly.backend.domain.dto.store.BuyStoreItemResponse;

/**
 * Mapper de dominio para el caso de uso de compra de un item de la tienda.
 */
public class BuyStoreItemMapper {

    public BuyStoreItemResponse toResponse(boolean purchased) {
        return new BuyStoreItemResponse(purchased);
    }
}
