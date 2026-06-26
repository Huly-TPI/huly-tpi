package com.huly.backend.domain.dto.badge;

import java.util.List;

/**
 * Respuesta de dominio con el listado de insignias disponibles.
 */
public record GetAllBadgesResponse(List<BadgeItem> badges) {
}
