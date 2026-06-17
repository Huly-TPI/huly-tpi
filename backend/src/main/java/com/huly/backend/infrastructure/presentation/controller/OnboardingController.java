package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.infrastructure.presentation.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsRequest;
import com.huly.backend.infrastructure.presentation.dto.onboarding.GenerateOptionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;
    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final CompleteTutorialUseCase completeTutorialUseCase;

    @PostMapping("/generate-options")
    public ResponseEntity<GenerateOptionsResponse> generateOptions(
            @Valid @RequestBody GenerateOptionsRequest request
    ) {
        List<String> options = generateOnboardingOptionsUseCase.execute(request.step(), request.previousAnswer());
        return ResponseEntity.ok(new GenerateOptionsResponse(options));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding(
            @Valid @RequestBody CompleteOnboardingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeOnboardingUseCase.execute(userId, request.answer1(), request.answer2(), request.answer3());
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/tutorial/complete")
    public ResponseEntity<Void> completeTutorial(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeTutorialUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profile-onboarding-tutorial/complete")
    public ResponseEntity<Void> completeProfileTutorial(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        completeTutorialUseCase.executeProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
