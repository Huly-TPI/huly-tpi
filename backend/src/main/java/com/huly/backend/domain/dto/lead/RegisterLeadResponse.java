package com.huly.backend.domain.dto.lead;

/**
 * Respuesta de dominio luego de registrar un lead.
 */
public record RegisterLeadResponse(
        Long id,
        String email,
        String nickname
) {
}
