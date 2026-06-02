package com.huly.backend.presentation.controller;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.presentation.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.presentation.dto.onboarding.GenerateOptionsRequest;
import com.huly.backend.presentation.dto.onboarding.GenerateOptionsResponse;
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
            @Valid @RequestBody GenerateOptionsRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<String> options = generateOnboardingOptionsUseCase.execute(request.step(), request.previousAnswer());
        return ResponseEntity.ok(new GenerateOptionsResponse(options));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding(
            @Valid @RequestBody CompleteOnboardingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        completeOnboardingUseCase.execute(userDetails.getUsername(), request.answer1(), request.answer2(), request.answer3());
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/tutorial/complete")
    public ResponseEntity<Void> completeTutorial(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        completeTutorialUseCase.execute(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
