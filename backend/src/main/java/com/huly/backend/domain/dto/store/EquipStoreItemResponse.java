package com.huly.backend.domain.dto.store;

/**
 * Respuesta de dominio luego de equipar un item de la tienda.
 *
 * @param equipped indica si el item quedo equipado.
 */
public record EquipStoreItemResponse(boolean equipped) {
}
