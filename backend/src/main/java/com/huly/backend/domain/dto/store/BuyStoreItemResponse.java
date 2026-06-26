package com.huly.backend.domain.dto.store;

/**
 * Respuesta de dominio luego de comprar un item de la tienda.
 *
 * @param purchased indica si el item fue comprado.
 */
public record BuyStoreItemResponse(boolean purchased) {
}
