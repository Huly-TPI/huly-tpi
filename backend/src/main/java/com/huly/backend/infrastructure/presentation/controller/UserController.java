package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import com.huly.backend.infrastructure.presentation.dto.user.CoinsResponse;
import com.huly.backend.infrastructure.presentation.dto.user.MembershipResponse;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateThemePreferenceRequest;
import jakarta.validation.Valid;
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
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final GetUserCoinsUseCase getUserCoinsUseCase;
    private final GetCurrentMembershipUseCase getCurrentMembershipUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Long userId = currentUserId(principal);
        UserProfile profile = getCurrentUserUseCase.execute(userId);
        ThemePreference themePreference = userDetailDomainRepository.findThemePreference(userId);

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(profile.user().getId())
                .name(profile.user().getName())
                .email(profile.user().getEmail())
                .role(profile.user().getRole())
                .onBoardingCompleted(profile.onBoardingCompleted())
                .onboardingTutorialCompleted(profile.onboardingTutorialCompleted())
                .themePreference(themePreference)
                .build());
    }

    @GetMapping("/me/coins")
    public ResponseEntity<CoinsResponse> getMyCoins(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = currentUserId(principal);
        int coins = getUserCoinsUseCase.execute(userId);
        return ResponseEntity.ok(new CoinsResponse(coins));
    }

    @GetMapping("/me/membership")
    public ResponseEntity<MembershipResponse> getMyMembership(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = currentUserId(principal);
        MembershipResponse response = getCurrentMembershipUseCase.execute(userId)
                .map(p -> new MembershipResponse(true, p.getPlanCode(), p.getExpiresAt()))
                .orElseGet(MembershipResponse::inactive);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/theme")
    public ResponseEntity<Void> updateTheme(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateThemePreferenceRequest request
    ) {
        Long userId = currentUserId(principal);
        userDetailDomainRepository.updateThemePreference(userId, request.themePreference());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
