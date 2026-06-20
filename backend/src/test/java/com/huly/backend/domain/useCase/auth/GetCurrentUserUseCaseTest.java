package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;

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

    @Mock private UserRepository userRepository;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    void execute_shouldReturnUserProfile_whenUserIdExists() {
        AppUser user = AppUser.builder()
                .id(1L).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.of(true));
        when(userDetailDomainRepository.findOnboardingTutorialCompleted(1L)).thenReturn(Optional.of(false));
        when(userDetailDomainRepository.findProfileOnboardingTutorialCompleted(1L)).thenReturn(Optional.of(true));

        UserProfile result = getCurrentUserUseCase.execute(1L);

        assertThat(result.user().getId()).isEqualTo(1L);
        assertThat(result.user().getName()).isEqualTo("Mili");
        assertThat(result.user().getEmail()).isEqualTo("user@huly.com");
        assertThat(result.user().getRole()).isEqualTo(UserRole.USER);
        assertThat(result.onBoardingCompleted()).isTrue();
        assertThat(result.onboardingTutorialCompleted()).isFalse();
        assertThat(result.profileOnboardingTutorialCompleted()).isTrue();
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
