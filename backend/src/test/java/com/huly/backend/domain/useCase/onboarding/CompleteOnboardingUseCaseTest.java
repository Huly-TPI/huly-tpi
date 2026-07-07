package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.badge.GrantBadgeRequest;
import com.huly.backend.domain.dto.onboarding.CompleteOnboardingRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.onboarding.CompleteOnboardingMapper;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteOnboardingUseCaseTest {

    private static final long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    @Mock
    private GrantBadgeUseCase grantBadgeUseCase;

    private CompleteOnboardingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteOnboardingUseCase(
                userRepository,
                userDetailDomainRepository,
                userVectorMemoryService,
                grantBadgeUseCase,
                new CompleteOnboardingMapper()
        );
    }

    @Test
    @DisplayName("Completa el onboarding y guarda la memoria vectorial cuando el usuario existe")
    void executeCompletesOnboardingWhenUserExists() {
        givenExistingUser(USER_ID);

        completeOnboarding(USER_ID, "Desestresarme", "Meditar", "Meditar 5 minutos");

        thenOnboardingCompleted(USER_ID, "Desestresarme", "Meditar", "Meditar 5 minutos");
        thenVectorMemorySavedForUser(USER_ID, "Desestresarme");
        thenBadgeGranted("user@huly.com");
    }

    @Test
    @DisplayName("Lanza no encontrado cuando el usuario no existe")
    void executeThrowsNotFoundWhenUserDoesNotExist() {
        givenUserNotFound(999L);

        thenCompleteOnboardingThrowsNotFound(999L, "A", "B", "C");
    }

    @Test
    @DisplayName("Completa el onboarding aunque falle la memoria vectorial")
    void executeCompletesOnboardingEvenIfVectorMemoryFails() {
        givenExistingUser(USER_ID);
        givenVectorMemoryFails();

        completeOnboarding(USER_ID, "A", "B", "C");

        thenOnboardingCompleted(USER_ID, "A", "B", "C");
    }

    @Test
    @DisplayName("Guarda la memoria con sourceId nulo cuando el usuario no tiene id")
    void executeSavesMemoryWithNullSourceIdWhenUserHasNoId() {
        givenExistingUserWithoutId();

        completeOnboarding(USER_ID, "A", "B", "C");

        thenVectorMemorySavedWithNullSource();
        thenBadgeGranted("user@huly.com");
    }

    // --- arrange ---

    private void givenExistingUser(long userId) {
        AppUser user = AppUser.builder()
                .id(userId)
                .email("user@huly.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void givenExistingUserWithoutId() {
        AppUser user = AppUser.builder()
                .email("user@huly.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void givenUserNotFound(long userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
    }

    private void givenVectorMemoryFails() {
        doThrow(new RuntimeException("Vector memory error"))
                .when(userVectorMemoryService).saveMemory(any(SaveVectorMemoryCommand.class));
    }

    // --- act ---

    private void completeOnboarding(long userId, String answer1, String answer2, String answer3) {
        useCase.execute(new CompleteOnboardingRequest(userId, answer1, answer2, answer3));
    }

    // --- assert ---

    private void thenOnboardingCompleted(long userId, String answer1, String answer2, String answer3) {
        verify(userDetailDomainRepository).completeOnboarding(userId, answer1, answer2, answer3);
    }

    private void thenVectorMemorySavedForUser(long userId, String contentFragment) {
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().content()).contains(contentFragment);
    }

    private void thenVectorMemorySavedWithNullSource() {
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isNull();
        assertThat(captor.getValue().sourceId()).isNull();
    }

    private void thenBadgeGranted(String email) {
        verify(grantBadgeUseCase).execute(new GrantBadgeRequest(email, "PRIMER_PASO"));
    }

    private void thenCompleteOnboardingThrowsNotFound(long userId, String answer1, String answer2, String answer3) {
        assertThatThrownBy(() -> useCase.execute(new CompleteOnboardingRequest(userId, answer1, answer2, answer3)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
