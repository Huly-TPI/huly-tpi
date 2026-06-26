package com.huly.backend.domain.dto.onboarding;

/**
 * Pedido de dominio para completar un tutorial de un usuario.
 *
 * @param userId usuario que completa el tutorial.
 */
public record CompleteTutorialRequest(Long userId) {
}
