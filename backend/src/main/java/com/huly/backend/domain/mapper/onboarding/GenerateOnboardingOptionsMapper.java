package com.huly.backend.domain.mapper.onboarding;

import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de generacion de opciones de onboarding.
 */
public class GenerateOnboardingOptionsMapper {

    public GenerateOnboardingOptionsResponse toResponse(List<String> options) {
        return new GenerateOnboardingOptionsResponse(options);
    }
}
