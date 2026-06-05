package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
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
    private final UserDetailDomainRepository userDetailDomainRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        AppUser user = getCurrentUserUseCase.execute(principal.getUsername());
        Boolean onBoardingCompleted = userDetailDomainRepository.findOnBoardingCompleted(user.getId()).orElse(false);
        Boolean onboardingTutorialCompleted = userDetailDomainRepository.findOnboardingTutorialCompleted(user.getId()).orElse(false);

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .onBoardingCompleted(onBoardingCompleted)
                .onboardingTutorialCompleted(onboardingTutorialCompleted)
                .build());
    }
}
