package com.huly.backend.domain.dto.activities;

import java.util.List;

/**
 * Respuesta de dominio con el listado de actividades disponibles.
 */
public record ListActivitiesResponse(List<ActivityItem> activities) {
}
