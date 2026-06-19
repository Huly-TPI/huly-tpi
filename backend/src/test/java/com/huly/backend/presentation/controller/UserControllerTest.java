package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import com.huly.backend.infrastructure.presentation.controller.UserController;
import com.huly.backend.infrastructure.presentation.dto.user.MembershipResponse;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateThemePreferenceRequest;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private GetCurrentUserUseCase getCurrentUserUseCase;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;
    @Mock private GetUserCoinsUseCase getUserCoinsUseCase;
    @Mock private GetCurrentMembershipUseCase getCurrentMembershipUseCase;

    @InjectMocks private UserController userController;

    private UserDetails principalWithId(Long id) {
        return new User(String.valueOf(id), "ignored", Collections.emptyList());
    }

    @Test
    void me_shouldReturnUserProfile_whenPrincipalIsValid() {
        AppUser user = AppUser.builder()
                .id(1L).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        UserProfile profile = new UserProfile(user, true, false, true);
        when(getCurrentUserUseCase.execute(1L)).thenReturn(profile);
        when(userDetailDomainRepository.findThemePreference(1L)).thenReturn(ThemePreference.DARK);

        ResponseEntity<UserProfileResponse> response =
                userController.me(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserProfileResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(1L);
        assertThat(body.getName()).isEqualTo("Mili");
        assertThat(body.getEmail()).isEqualTo("user@huly.com");
        assertThat(body.getRole()).isEqualTo(UserRole.USER);
        assertThat(body.getOnBoardingCompleted()).isTrue();
        assertThat(body.getOnboardingTutorialCompleted()).isFalse();
        assertThat(body.getProfileOnboardingTutorialCompleted()).isTrue();
        assertThat(body.getThemePreference()).isEqualTo(ThemePreference.DARK);
    }

    @Test
    void updateTheme_shouldPersistThemePreference_whenPrincipalIsValid() {
        ResponseEntity<Void> response = userController.updateTheme(
                principalWithId(1L),
                new UpdateThemePreferenceRequest(ThemePreference.DARK)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userDetailDomainRepository).updateThemePreference(1L, ThemePreference.DARK);
    }

    @Test
    void updateTheme_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy(() -> userController.updateTheme(null, new UpdateThemePreferenceRequest(ThemePreference.DARK)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void me_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy(() -> userController.me(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void getMyCoins_shouldReturnCoins_whenPrincipalIsValid() {
        when(getUserCoinsUseCase.execute(1L)).thenReturn(750);

        ResponseEntity<com.huly.backend.infrastructure.presentation.dto.user.CoinsResponse> response =
                userController.getMyCoins(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().coins()).isEqualTo(750);
    }

    @Test
    void getMyCoins_shouldReturnZero_whenUserHasNoCoins() {
        when(getUserCoinsUseCase.execute(1L)).thenReturn(0);

        ResponseEntity<com.huly.backend.infrastructure.presentation.dto.user.CoinsResponse> response =
                userController.getMyCoins(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().coins()).isZero();
    }

    @Test
    void getMyMembership_shouldReturnActiveMembership_whenUserHasOne() {
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
        UserPlan plan = UserPlan.builder()
                .id(1L).userId(1L).productId(7L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(expiresAt)
                .build();
        when(getCurrentMembershipUseCase.execute(1L)).thenReturn(Optional.of(plan));

        ResponseEntity<MembershipResponse> response =
                userController.getMyMembership(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().active()).isTrue();
        assertThat(response.getBody().planCode()).isEqualTo("PREMIUM");
        assertThat(response.getBody().productId()).isEqualTo("7");
        assertThat(response.getBody().expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void getMyMembership_shouldReturnInactive_whenUserHasNoMembership() {
        when(getCurrentMembershipUseCase.execute(1L)).thenReturn(Optional.empty());

        ResponseEntity<MembershipResponse> response =
                userController.getMyMembership(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().active()).isFalse();
        assertThat(response.getBody().planCode()).isNull();
        assertThat(response.getBody().productId()).isNull();
        assertThat(response.getBody().expiresAt()).isNull();
    }
}
