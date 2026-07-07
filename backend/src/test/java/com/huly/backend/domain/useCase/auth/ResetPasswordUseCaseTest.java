package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    private static final String VALID_TOKEN = "valid-uuid-token";
    private static final String MISSING_TOKEN = "nonexistent";
    private static final String NEW_PASSWORD = "newPass123";
    private static final String ENCODED_PASSWORD = "encodedNewPass";
    private static final Long USER_ID = 1L;

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordHasherPort passwordHasherPort;

    private ResetPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(userRepository, passwordResetTokenRepository, passwordHasherPort);
    }

    @Test
    @DisplayName("Actualiza la contraseña y borra el token cuando el token es válido")
    void executeShouldUpdatePasswordAndDeleteTokenWhenTokenIsValid() {
        PasswordResetToken token = validToken();
        givenTokenFound(token);
        givenPasswordEncoded();

        resetPassword();

        thenUpdatedPasswordAndDeletedToken(token);
    }

    @Test
    @DisplayName("Codifica la contraseña antes de actualizarla")
    void executeShouldEncodePasswordBeforeUpdating() {
        givenTokenFound(validToken());
        givenPasswordEncoded();

        resetPassword();

        thenUpdatedPasswordWithEncodedValue();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el token no existe")
    void executeShouldThrowResourceNotFoundWhenTokenDoesNotExist() {
        givenTokenNotFound();

        thenResetPasswordThrowsForMissingToken();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el token expiró")
    void executeShouldThrowResourceNotFoundWhenTokenIsExpired() {
        givenTokenFound(expiredToken());

        thenResetPasswordThrowsForValidToken();
    }

    @Test
    @DisplayName("Borra el token expirado cuando el token expiró")
    void executeShouldDeleteExpiredTokenWhenTokenIsExpired() {
        PasswordResetToken expired = expiredToken();
        givenTokenFound(expired);

        thenResetPasswordThrowsForValidToken();
        thenDeletedToken(expired);
    }

    @Test
    @DisplayName("No actualiza la contraseña cuando el token expiró")
    void executeShouldNeverUpdatePasswordWhenTokenIsExpired() {
        givenTokenFound(expiredToken());

        thenResetPasswordThrowsForValidToken();
        thenNeverUpdatedPassword();
    }

    @Test
    @DisplayName("No actualiza la contraseña cuando el token no existe")
    void executeShouldNeverUpdatePasswordWhenTokenDoesNotExist() {
        givenTokenNotFound();

        thenResetPasswordThrowsForMissingToken();
        thenNeverUpdatedPassword();
    }

    // --- arrange ---

    private void givenTokenFound(PasswordResetToken token) {
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
    }

    private void givenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken(MISSING_TOKEN)).thenReturn(Optional.empty());
    }

    private void givenPasswordEncoded() {
        when(passwordHasherPort.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
    }

    private PasswordResetToken buildToken(Instant expiresAt) {
        return PasswordResetToken.builder()
                .id(10L)
                .userId(USER_ID)
                .token(VALID_TOKEN)
                .createdAt(Instant.now().minusSeconds(60))
                .expiresAt(expiresAt)
                .build();
    }

    private PasswordResetToken validToken() {
        return buildToken(Instant.now().plusSeconds(900));
    }

    private PasswordResetToken expiredToken() {
        return buildToken(Instant.now().minusSeconds(1));
    }

    // --- act ---

    private void resetPassword() {
        useCase.execute(VALID_TOKEN, NEW_PASSWORD);
    }

    // --- assert ---

    private void thenUpdatedPasswordAndDeletedToken(PasswordResetToken token) {
        verify(userRepository).updatePassword(USER_ID, ENCODED_PASSWORD);
        verify(passwordResetTokenRepository).delete(token);
    }

    private void thenUpdatedPasswordWithEncodedValue() {
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).updatePassword(eq(USER_ID), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo(ENCODED_PASSWORD);
    }

    private void thenResetPasswordThrowsForValidToken() {
        assertThatThrownBy(() -> useCase.execute(VALID_TOKEN, NEW_PASSWORD))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenResetPasswordThrowsForMissingToken() {
        assertThatThrownBy(() -> useCase.execute(MISSING_TOKEN, NEW_PASSWORD))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenDeletedToken(PasswordResetToken token) {
        verify(passwordResetTokenRepository).delete(token);
    }

    private void thenNeverUpdatedPassword() {
        verify(userRepository, never()).updatePassword(any(), any());
    }
}
