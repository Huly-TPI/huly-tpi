package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailPort emailPort;

    private RequestPasswordResetUseCase useCase;

    private static final String EMAIL = "user@huly.com";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        useCase = new RequestPasswordResetUseCase(userRepository, passwordResetTokenRepository, emailPort);
    }

    @Test
    void execute_shouldSaveTokenAndSendEmail_whenEmailExists() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(EMAIL);

        verify(passwordResetTokenRepository).deleteAllByUserId(USER_ID);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailPort).sendPasswordReset(any(), any());
    }

    @Test
    void execute_shouldSendEmailToCorrectAddress() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(EMAIL);

        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).sendPasswordReset(toCaptor.capture(), any());
        assertThat(toCaptor.getValue()).isEqualTo(EMAIL);
    }

    @Test
    void execute_shouldSaveTokenWithCorrectUserId() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(passwordResetTokenRepository.save(tokenCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(EMAIL);

        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());
    }

    @Test
    void execute_shouldDeletePreviousTokensBeforeSavingNew() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(EMAIL);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(passwordResetTokenRepository);
        inOrder.verify(passwordResetTokenRepository).deleteAllByUserId(USER_ID);
        inOrder.verify(passwordResetTokenRepository).save(any());
    }

    @Test
    void execute_shouldThrowResourceNotFound_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing@huly.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldNeverSendEmail_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing@huly.com"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(emailPort, never()).sendPasswordReset(any(), any());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    private AppUser buildUser() {
        return AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("encodedPass")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
