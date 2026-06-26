package com.huly.backend.domain.mapper.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteTutorialResponse;

/**
 * Mapper de dominio para el caso de uso de completar un tutorial.
 */
public class CompleteTutorialMapper {

    public CompleteTutorialResponse toResponse() {
        return new CompleteTutorialResponse();
    }
}
