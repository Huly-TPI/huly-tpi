package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteOnboardingUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;
    @Mock private UserVectorMemoryService userVectorMemoryService;

    @InjectMocks private CompleteOnboardingUseCase completeOnboardingUseCase;

    @Mock
    private GrantBadgeUseCase grantBadgeUseCase;

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        completeOnboardingUseCase.execute(1L, "Desestresarme", "Meditar", "Meditar 5 minutos");

        verify(userDetailDomainRepository).completeOnboarding(1L, "Desestresarme", "Meditar", "Meditar 5 minutos");
        
        org.mockito.ArgumentCaptor<SaveVectorMemoryCommand> captor =
                org.mockito.ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
        assertThat(captor.getValue().content()).contains("Desestresarme");
    }

    @Test
    void execute_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeOnboardingUseCase.execute(999L, "A", "B", "C"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldCompleteOnboarding_evenIfVectorMemoryFails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Vector memory error"))
                .when(userVectorMemoryService).saveMemory(any(SaveVectorMemoryCommand.class));

        completeOnboardingUseCase.execute(1L, "A", "B", "C");

        verify(userDetailDomainRepository).completeOnboarding(1L, "A", "B", "C");
    }
}
