package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long MISSING_USER_ID = 999L;

    @Mock private UserRepository userRepository;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    @DisplayName("Devuelve el perfil con los flags de onboarding cuando el usuario existe")
    void executeShouldReturnUserProfileWhenUserIdExists() {
        givenUserFound();
        givenOnboardingFlags(true, false, true);

        UserProfile result = getCurrentUser(USER_ID);

        thenProfileMatches(result);
    }

    @Test
    @DisplayName("Deja los flags de onboarding en false cuando no hay detalle del usuario")
    void executeShouldDefaultFlagsToFalseWhenUserDetailAbsent() {
        givenUserFound();
        givenOnboardingFlagsAbsent();

        UserProfile result = getCurrentUser(USER_ID);

        thenAllFlagsFalse(result);
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el usuario no existe")
    void executeShouldThrowResourceNotFoundWhenUserNotFound() {
        givenUserNotFound();

        thenGetCurrentUserThrowsResourceNotFound();
    }

    // --- arrange ---

    private void givenUserFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
    }

    private void givenUserNotFound() {
        when(userRepository.findById(MISSING_USER_ID)).thenReturn(Optional.empty());
    }

    private void givenOnboardingFlags(boolean onBoarding, boolean tutorial, boolean profileTutorial) {
        when(userDetailDomainRepository.findOnBoardingCompleted(USER_ID)).thenReturn(Optional.of(onBoarding));
        when(userDetailDomainRepository.findOnboardingTutorialCompleted(USER_ID)).thenReturn(Optional.of(tutorial));
        when(userDetailDomainRepository.findProfileOnboardingTutorialCompleted(USER_ID)).thenReturn(Optional.of(profileTutorial));
    }

    private void givenOnboardingFlagsAbsent() {
        when(userDetailDomainRepository.findOnBoardingCompleted(USER_ID)).thenReturn(Optional.empty());
        when(userDetailDomainRepository.findOnboardingTutorialCompleted(USER_ID)).thenReturn(Optional.empty());
        when(userDetailDomainRepository.findProfileOnboardingTutorialCompleted(USER_ID)).thenReturn(Optional.empty());
    }

    private AppUser user() {
        return AppUser.builder()
                .id(USER_ID).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    // --- act ---

    private UserProfile getCurrentUser(Long userId) {
        return getCurrentUserUseCase.execute(userId);
    }

    // --- assert ---

    private void thenProfileMatches(UserProfile result) {
        assertThat(result.user().getId()).isEqualTo(USER_ID);
        assertThat(result.user().getName()).isEqualTo("Mili");
        assertThat(result.user().getEmail()).isEqualTo("user@huly.com");
        assertThat(result.user().getRole()).isEqualTo(UserRole.USER);
        assertThat(result.onBoardingCompleted()).isTrue();
        assertThat(result.onboardingTutorialCompleted()).isFalse();
        assertThat(result.profileOnboardingTutorialCompleted()).isTrue();
    }

    private void thenAllFlagsFalse(UserProfile result) {
        assertThat(result.onBoardingCompleted()).isFalse();
        assertThat(result.onboardingTutorialCompleted()).isFalse();
        assertThat(result.profileOnboardingTutorialCompleted()).isFalse();
    }

    private void thenGetCurrentUserThrowsResourceNotFound() {
        assertThatThrownBy(() -> getCurrentUserUseCase.execute(MISSING_USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
