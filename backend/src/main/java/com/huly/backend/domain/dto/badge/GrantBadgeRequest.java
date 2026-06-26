package com.huly.backend.domain.dto.badge;

/**
 * Pedido de dominio para otorgar una insignia a un usuario.
 *
 * @param email     email del usuario al que se le otorga la insignia.
 * @param badgeCode codigo de la insignia a otorgar.
 */
public record GrantBadgeRequest(String email, String badgeCode) {
}
