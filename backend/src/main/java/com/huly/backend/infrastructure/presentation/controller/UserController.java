package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Long userId = Long.parseLong(principal.getUsername());
        UserProfile profile = getCurrentUserUseCase.execute(userId);

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(profile.user().getId())
                .name(profile.user().getName())
                .email(profile.user().getEmail())
                .role(profile.user().getRole())
                .onBoardingCompleted(profile.onBoardingCompleted())
                .onboardingTutorialCompleted(profile.onboardingTutorialCompleted())
                .build());
    }
}
