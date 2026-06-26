package com.huly.backend.domain.dto.onboarding;

import java.util.List;

/**
 * Respuesta de dominio con las opciones de onboarding generadas.
 */
public record GenerateOnboardingOptionsResponse(List<String> options) {
}
