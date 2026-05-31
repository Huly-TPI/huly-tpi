package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteProfileOnboardingUseCaseTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;
    @InjectMocks
    private CompleteProfileOnboardingUseCase completeProfileOnboardingUseCase;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(1L)
                .email("user@huly.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_shouldCallCompleteOnboarding_whenUserExists() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        completeProfileOnboardingUseCase.execute("user@huly.com", "Desestresarme", "Meditar", "Meditar 5 minutos");
        verify(userDetailDomainRepository).completeOnboarding(1L, "Desestresarme", "Meditar", "Meditar 5 minutos");
    }

    @Test
    void execute_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("noexiste@huly.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> completeProfileOnboardingUseCase.execute("noexiste@huly.com", "A", "B", "C"))
                .isInstanceOf(NotFoundException.class);

    }
}
