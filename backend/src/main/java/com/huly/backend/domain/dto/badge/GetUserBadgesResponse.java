package com.huly.backend.domain.dto.badge;

import java.util.List;

/**
 * Respuesta de dominio con el listado de insignias obtenidas por el usuario.
 */
public record GetUserBadgesResponse(List<UserBadgeItem> badges) {
}
