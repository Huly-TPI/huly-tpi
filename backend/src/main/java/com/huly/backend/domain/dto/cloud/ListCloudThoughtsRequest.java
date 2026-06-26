package com.huly.backend.domain.dto.cloud;

/**
 * Pedido de dominio para listar los pensamientos de nube de un usuario.
 *
 * @param userId usuario dueño de los pensamientos.
 */
public record ListCloudThoughtsRequest(Long userId) {
}
