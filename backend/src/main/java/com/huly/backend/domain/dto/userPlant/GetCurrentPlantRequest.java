package com.huly.backend.domain.dto.userPlant;

/**
 * Pedido de dominio para obtener (o crear) la planta actual del usuario.
 */
public record GetCurrentPlantRequest(Long userId) {
}
