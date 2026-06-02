package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompleteTutorialUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks
    private CompleteTutorialUseCase completeTutorialUseCase;

    @Test
    void execute_shouldCompleteTutorial() {
        AppUser user = AppUser.builder().id(1L).email("user@huly.com").build();
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));

        completeTutorialUseCase.execute("user@huly.com");

        verify(userDetailDomainRepository).completeTutorial(1L);
    }

    @Test
    void execute_shouldThrowNotFound_whenUserDetailDoesNotExist() {
        AppUser user = AppUser.builder().id(99L).email("missing-detail@huly.com").build();
        when(userRepository.findByEmail("missing-detail@huly.com")).thenReturn(Optional.of(user));
        org.mockito.Mockito.doThrow(new NotFoundException("No se encontraron datos del usuario"))
                .when(userDetailDomainRepository).completeTutorial(99L);

        assertThatThrownBy(() -> completeTutorialUseCase.execute("missing-detail@huly.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No se encontraron datos del usuario");
    }

    @Test
    void execute_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeTutorialUseCase.execute("missing@huly.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
