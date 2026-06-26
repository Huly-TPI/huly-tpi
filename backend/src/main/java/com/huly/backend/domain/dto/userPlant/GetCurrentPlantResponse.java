package com.huly.backend.domain.dto.userPlant;

/**
 * Respuesta de dominio con la planta actual del usuario.
 */
public record GetCurrentPlantResponse(UserPlantItem plant) {
}
