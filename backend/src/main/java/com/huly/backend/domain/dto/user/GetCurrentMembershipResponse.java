package com.huly.backend.domain.dto.user;

import java.time.Instant;

/**
 * Respuesta de dominio con la membresia vigente de un usuario.
 * Si {@code active} es false, el resto de los campos son nulos.
 */
public record GetCurrentMembershipResponse(
        boolean active,
        String planCode,
        Long productId,
        Instant expiresAt
) {

    public static GetCurrentMembershipResponse inactive() {
        return new GetCurrentMembershipResponse(false, null, null, null);
    }
}
