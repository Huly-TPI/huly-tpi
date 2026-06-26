package com.huly.backend.domain.dto.userPlant;

import java.util.List;

/**
 * Respuesta de dominio con el listado de plantas de un usuario.
 */
public record GetUserPlantsResponse(List<UserPlantItem> plants) {
}
