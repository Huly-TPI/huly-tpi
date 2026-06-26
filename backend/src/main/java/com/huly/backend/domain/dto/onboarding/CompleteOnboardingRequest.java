package com.huly.backend.domain.dto.onboarding;

/**
 * Pedido de dominio para completar el onboarding de un usuario.
 *
 * @param userId  usuario que completa el onboarding.
 * @param answer1 primera respuesta.
 * @param answer2 segunda respuesta.
 * @param answer3 tercera respuesta.
 */
public record CompleteOnboardingRequest(Long userId, String answer1, String answer2, String answer3) {
}
