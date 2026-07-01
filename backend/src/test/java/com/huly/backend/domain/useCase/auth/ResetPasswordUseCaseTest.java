package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordHasherPort passwordHasherPort;

    private ResetPasswordUseCase useCase;

    private static final String VALID_TOKEN = "valid-uuid-token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(userRepository, passwordResetTokenRepository, passwordHasherPort);
    }

    @Test
    void execute_shouldUpdatePasswordAndDeleteToken_whenTokenIsValid() {
        PasswordResetToken token = buildToken(Instant.now().plusSeconds(900));
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
        when(passwordHasherPort.encode("newPass123")).thenReturn("encodedNewPass");

        useCase.execute(VALID_TOKEN, "newPass123");

        verify(userRepository).updatePassword(USER_ID, "encodedNewPass");
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    void execute_shouldEncodePasswordBeforeUpdating() {
        PasswordResetToken token = buildToken(Instant.now().plusSeconds(900));
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
        when(passwordHasherPort.encode("newPass123")).thenReturn("encodedNewPass");

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        useCase.execute(VALID_TOKEN, "newPass123");

        verify(userRepository).updatePassword(eq(USER_ID), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo("encodedNewPass");
    }

    @Test
    void execute_shouldThrowResourceNotFound_whenTokenDoesNotExist() {
        when(passwordResetTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("nonexistent", "newPass123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldThrowResourceNotFound_whenTokenIsExpired() {
        PasswordResetToken expiredToken = buildToken(Instant.now().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> useCase.execute(VALID_TOKEN, "newPass123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldDeleteExpiredToken_whenTokenIsExpired() {
        PasswordResetToken expiredToken = buildToken(Instant.now().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> useCase.execute(VALID_TOKEN, "newPass123"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordResetTokenRepository).delete(expiredToken);
    }

    @Test
    void execute_shouldNeverUpdatePassword_whenTokenIsExpired() {
        PasswordResetToken expiredToken = buildToken(Instant.now().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> useCase.execute(VALID_TOKEN, "newPass123"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).updatePassword(any(), any());
    }

    @Test
    void execute_shouldNeverUpdatePassword_whenTokenDoesNotExist() {
        when(passwordResetTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("nonexistent", "newPass123"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).updatePassword(any(), any());
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
}
