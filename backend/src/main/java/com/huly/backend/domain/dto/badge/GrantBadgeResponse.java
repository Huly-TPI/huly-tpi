package com.huly.backend.domain.dto.badge;

/**
 * Respuesta de dominio luego de intentar otorgar una insignia a un usuario.
 *
 * @param granted true si la insignia fue otorgada en esta operacion,
 *                false si el usuario ya la poseia.
 */
public record GrantBadgeResponse(boolean granted) {
}
