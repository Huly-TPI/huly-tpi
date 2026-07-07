package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    private static final long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    private ChangePasswordUseCase useCase;

    private AppUser user;

    @BeforeEach
    void setUp() {
        useCase = new ChangePasswordUseCase(userRepository, passwordHasherPort);
        user = AppUser.builder()
                .id(USER_ID)
                .email("user@huly.com")
                .password("encodedCurrentPass")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Actualiza la contraseña cuando la actual es correcta")
    void executeUpdatesPasswordWhenCurrentPasswordIsCorrect() {
        givenExistingUser();
        givenCurrentPasswordMatches();
        givenEncodedNewPassword();

        changePassword(USER_ID, "currentPass", "newPass123");

        thenPasswordUpdatedTo("encodedNewPass");
    }

    @Test
    @DisplayName("Codifica la nueva contraseña antes de guardarla")
    void executeEncodesNewPasswordBeforeSaving() {
        givenExistingUser();
        givenCurrentPasswordMatches();
        givenEncodedNewPassword();

        changePassword(USER_ID, "currentPass", "newPass123");

        thenSavedPasswordIs("encodedNewPass");
    }

    @Test
    @DisplayName("Lanza credenciales inválidas cuando el usuario no existe")
    void executeThrowsInvalidCredentialsWhenUserNotFound() {
        givenUserNotFound(99L);

        thenChangePasswordThrowsInvalidCredentials(99L, "currentPass", "newPass123");
    }

    @Test
    @DisplayName("Lanza credenciales inválidas cuando la contraseña actual es incorrecta")
    void executeThrowsInvalidCredentialsWhenCurrentPasswordIsWrong() {
        givenExistingUser();
        givenCurrentPasswordDoesNotMatch();

        thenChangePasswordThrowsIncorrectCurrentPassword(USER_ID, "wrongPass", "newPass123");
    }

    @Test
    @DisplayName("No actualiza la contraseña cuando la actual es incorrecta")
    void executeNeverUpdatesPasswordWhenCurrentPasswordIsWrong() {
        givenExistingUser();
        givenCurrentPasswordDoesNotMatch();

        thenChangePasswordThrowsInvalidCredentials(USER_ID, "wrongPass", "newPass123");
        thenPasswordWasNeverUpdated();
    }

    // --- arrange ---

    private void givenExistingUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private void givenUserNotFound(long userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
    }

    private void givenCurrentPasswordMatches() {
        when(passwordHasherPort.matches("currentPass", "encodedCurrentPass")).thenReturn(true);
    }

    private void givenCurrentPasswordDoesNotMatch() {
        when(passwordHasherPort.matches("wrongPass", "encodedCurrentPass")).thenReturn(false);
    }

    private void givenEncodedNewPassword() {
        when(passwordHasherPort.encode("newPass123")).thenReturn("encodedNewPass");
    }

    // --- act ---

    private void changePassword(long userId, String currentPassword, String newPassword) {
        useCase.execute(userId, currentPassword, newPassword);
    }

    // --- assert ---

    private void thenPasswordUpdatedTo(String expectedEncoded) {
        verify(userRepository).updatePassword(USER_ID, expectedEncoded);
    }

    private void thenSavedPasswordIs(String expectedEncoded) {
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).updatePassword(eq(USER_ID), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo(expectedEncoded);
    }

    private void thenChangePasswordThrowsInvalidCredentials(long userId, String currentPassword, String newPassword) {
        assertThatThrownBy(() -> useCase.execute(userId, currentPassword, newPassword))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private void thenChangePasswordThrowsIncorrectCurrentPassword(long userId, String currentPassword, String newPassword) {
        assertThatThrownBy(() -> useCase.execute(userId, currentPassword, newPassword))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("incorrect");
    }

    private void thenPasswordWasNeverUpdated() {
        verify(userRepository, never()).updatePassword(any(), any());
    }
}
