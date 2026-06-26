package com.huly.backend.domain.dto.store;

import java.util.List;

/**
 * Respuesta de dominio con el listado de items disponibles en la tienda.
 */
public record ListStoreItemsResponse(List<StoreItemView> items) {
}
