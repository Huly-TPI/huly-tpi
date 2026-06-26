package com.huly.backend.domain.dto.store;

import java.util.List;

/**
 * Respuesta de dominio con el inventario de items del usuario.
 */
public record GetUserInventoryResponse(List<InventoryItemView> items) {
}
