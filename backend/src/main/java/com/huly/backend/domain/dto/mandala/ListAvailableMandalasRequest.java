package com.huly.backend.domain.dto.mandala;

/**
 * Pedido de dominio para listar los mandalas disponibles de forma paginada.
 *
 * @param userId usuario que solicita el listado.
 * @param page   numero de pagina solicitado.
 * @param size   cantidad de elementos por pagina.
 */
public record ListAvailableMandalasRequest(Long userId, int page, int size) {
}
