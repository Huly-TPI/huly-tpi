package com.huly.backend.domain.dto.cloud;

/**
 * Pedido de dominio para marcar un pensamiento de nube como trabajado.
 *
 * @param id     identificador del pensamiento.
 * @param userId usuario dueño del pensamiento.
 */
public record MarkCloudWorkedOnRequest(Long id, Long userId) {
}
