package com.huly.backend.domain.dto.onboarding;

/**
 * Pedido de dominio para generar opciones de onboarding.
 *
 * @param step           paso del onboarding.
 * @param previousAnswer respuesta previa del usuario.
 */
public record GenerateOnboardingOptionsRequest(Integer step, String previousAnswer) {
}
