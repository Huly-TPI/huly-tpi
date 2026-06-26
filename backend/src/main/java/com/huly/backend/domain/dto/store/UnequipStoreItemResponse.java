package com.huly.backend.domain.dto.store;

/**
 * Respuesta de dominio luego de desequipar un item de la tienda.
 *
 * @param unequipped indica si el item quedo desequipado.
 */
public record UnequipStoreItemResponse(boolean unequipped) {
}
