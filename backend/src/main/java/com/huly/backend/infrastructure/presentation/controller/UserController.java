package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        ThemePreference themePreference = userDetailDomainRepository.findThemePreference(user.getId());

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .onBoardingCompleted(onBoardingCompleted)
                .onboardingTutorialCompleted(onboardingTutorialCompleted)
                .build());
    }

    @PutMapping("/me/theme")
    public ResponseEntity<Void> updateTheme(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateThemePreferenceRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        AppUser user = getCurrentUserUseCase.execute(principal.getUsername());
        userDetailDomainRepository.updateThemePreference(user.getId(), request.themePreference());
        return ResponseEntity.noContent().build();
    }
}
