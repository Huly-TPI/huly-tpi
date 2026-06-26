package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteTutorialRequest;
import com.huly.backend.domain.mapper.onboarding.CompleteTutorialMapper;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompleteTutorialUseCaseTest {

    @Mock private UserDetailDomainRepository userDetailDomainRepository;

    private CompleteTutorialUseCase completeTutorialUseCase;

    @BeforeEach
    void setUp() {
        completeTutorialUseCase = new CompleteTutorialUseCase(userDetailDomainRepository, new CompleteTutorialMapper());
    }

    @Test
    void execute_shouldCompleteTutorial() {
        completeTutorialUseCase.execute(new CompleteTutorialRequest(1L));

        verify(userDetailDomainRepository).completeTutorial(1L);
    }

    @Test
    void executeProfile_shouldCompleteProfileTutorial() {
        completeTutorialUseCase.executeProfile(new CompleteTutorialRequest(1L));

        verify(userDetailDomainRepository).completeProfileTutorial(1L);
    }

    @Test
    void execute_shouldPropagateNotFound_whenUserDetailDoesNotExist() {
        doThrow(new NotFoundException("No se encontraron datos del usuario"))
                .when(userDetailDomainRepository).completeTutorial(99L);

        assertThatThrownBy(() -> completeTutorialUseCase.execute(new CompleteTutorialRequest(99L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No se encontraron datos del usuario");
    }
}
