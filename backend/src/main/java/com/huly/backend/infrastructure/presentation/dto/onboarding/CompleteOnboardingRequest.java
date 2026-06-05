package com.huly.backend.infrastructure.presentation.dto.onboarding;

import jakarta.validation.constraints.NotBlank;

public record CompleteOnboardingRequest(
    @NotBlank String answer1,
    @NotBlank String answer2,
    @NotBlank String answer3
) {
}
