package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteTutorialRequest;
import com.huly.backend.domain.dto.onboarding.CompleteTutorialResponse;
import com.huly.backend.domain.mapper.onboarding.CompleteTutorialMapper;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompleteTutorialUseCaseTest {

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    private CompleteTutorialUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteTutorialUseCase(userDetailDomainRepository, new CompleteTutorialMapper());
    }

    @Test
    @DisplayName("Completa el tutorial general del usuario")
    void executeCompletesTutorial() {
        CompleteTutorialResponse result = completeTutorial(1L);

        thenTutorialCompleted(1L);
        thenResponseIsNotNull(result);
    }

    @Test
    @DisplayName("Completa el tutorial de perfil del usuario")
    void executeProfileCompletesProfileTutorial() {
        CompleteTutorialResponse result = completeProfileTutorial(1L);

        thenProfileTutorialCompleted(1L);
        thenResponseIsNotNull(result);
    }

    @Test
    @DisplayName("Propaga no encontrado cuando no existen datos del usuario")
    void executePropagatesNotFoundWhenUserDetailDoesNotExist() {
        givenCompleteTutorialFails(99L);

        thenCompleteTutorialThrowsNotFound(99L);
    }

    // --- arrange ---

    private void givenCompleteTutorialFails(long userId) {
        doThrow(new NotFoundException("No se encontraron datos del usuario"))
                .when(userDetailDomainRepository).completeTutorial(userId);
    }

    // --- act ---

    private CompleteTutorialResponse completeTutorial(long userId) {
        return useCase.execute(new CompleteTutorialRequest(userId));
    }

    private CompleteTutorialResponse completeProfileTutorial(long userId) {
        return useCase.executeProfile(new CompleteTutorialRequest(userId));
    }

    // --- assert ---

    private void thenTutorialCompleted(long userId) {
        verify(userDetailDomainRepository).completeTutorial(userId);
    }

    private void thenProfileTutorialCompleted(long userId) {
        verify(userDetailDomainRepository).completeProfileTutorial(userId);
    }

    private void thenResponseIsNotNull(CompleteTutorialResponse result) {
        assertThat(result).isNotNull();
    }

    private void thenCompleteTutorialThrowsNotFound(long userId) {
        assertThatThrownBy(() -> useCase.execute(new CompleteTutorialRequest(userId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No se encontraron datos del usuario");
    }
}
