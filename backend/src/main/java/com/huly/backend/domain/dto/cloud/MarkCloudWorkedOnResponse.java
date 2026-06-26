package com.huly.backend.domain.dto.cloud;

/**
 * Respuesta de dominio luego de marcar un pensamiento de nube como trabajado.
 * El controlador la ignora; existe para no devolver {@code void}.
 *
 * @param id identificador del pensamiento marcado.
 */
public record MarkCloudWorkedOnResponse(Long id) {
}
