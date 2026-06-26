package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.infrastructure.presentation.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsRequest;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsResponse;
import com.huly.backend.infrastructure.presentation.mapper.onboarding.OnboardingPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;
    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final CompleteTutorialUseCase completeTutorialUseCase;
    private final OnboardingPresentationMapper onboardingPresentationMapper;

    @PostMapping("/generate-options")
    public ResponseEntity<GenerateOptionsResponse> generateOptions(
            @Valid @RequestBody GenerateOptionsRequest request
    ) {
        GenerateOnboardingOptionsResponse options = generateOnboardingOptionsUseCase.execute(
                onboardingPresentationMapper.toGenerateOptionsRequest(request));
        return ResponseEntity.ok(onboardingPresentationMapper.toGenerateOptionsResponse(options));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding(
            @Valid @RequestBody CompleteOnboardingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeOnboardingUseCase.execute(
                onboardingPresentationMapper.toCompleteOnboardingRequest(userId, request));
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/tutorial/complete")
    public ResponseEntity<Void> completeTutorial(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeTutorialUseCase.execute(
                onboardingPresentationMapper.toCompleteTutorialRequest(userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profile-onboarding-tutorial/complete")
    public ResponseEntity<Void> completeProfileTutorial(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeTutorialUseCase.executeProfile(
                onboardingPresentationMapper.toCompleteTutorialRequest(userId));
        return ResponseEntity.noContent().build();
    }
}
