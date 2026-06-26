package com.huly.backend.domain.mapper.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteOnboardingResponse;

/**
 * Mapper de dominio para el caso de uso de completar el onboarding.
 */
public class CompleteOnboardingMapper {

    public CompleteOnboardingResponse toResponse() {
        return new CompleteOnboardingResponse();
    }
}
