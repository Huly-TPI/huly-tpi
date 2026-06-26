package com.huly.backend.infrastructure.presentation.mapper.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.domain.dto.onboarding.CompleteTutorialRequest;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsRequest;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsRequest;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de onboarding:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class OnboardingPresentationMapper {

    public GenerateOnboardingOptionsRequest toGenerateOptionsRequest(GenerateOptionsRequest request) {
        return new GenerateOnboardingOptionsRequest(request.step(), request.previousAnswer());
    }

    public GenerateOptionsResponse toGenerateOptionsResponse(GenerateOnboardingOptionsResponse response) {
        return new GenerateOptionsResponse(response.options());
    }

    public CompleteOnboardingRequest toCompleteOnboardingRequest(
            Long userId,
            com.huly.backend.infrastructure.presentation.dto.onboarding.CompleteOnboardingRequest request
    ) {
        return new CompleteOnboardingRequest(userId, request.answer1(), request.answer2(), request.answer3());
    }

    public CompleteTutorialRequest toCompleteTutorialRequest(Long userId) {
        return new CompleteTutorialRequest(userId);
    }
}
