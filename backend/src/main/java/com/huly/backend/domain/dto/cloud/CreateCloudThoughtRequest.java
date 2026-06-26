package com.huly.backend.domain.dto.cloud;

/**
 * Pedido de dominio para crear un pensamiento de nube.
 *
 * @param userId usuario que crea el pensamiento.
 * @param text   contenido del pensamiento.
 */
public record CreateCloudThoughtRequest(Long userId, String text) {
}
