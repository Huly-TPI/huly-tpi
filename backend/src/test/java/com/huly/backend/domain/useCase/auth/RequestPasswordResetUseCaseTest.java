package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    private static final String EMAIL = "user@huly.com";
    private static final String MISSING_EMAIL = "missing@huly.com";
    private static final Long USER_ID = 1L;

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailPort emailPort;

    private RequestPasswordResetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestPasswordResetUseCase(userRepository, passwordResetTokenRepository, emailPort);
    }

    @Test
    @DisplayName("Borra los tokens previos, guarda uno nuevo y envía el email cuando el email existe")
    void executeShouldSaveTokenAndSendEmailWhenEmailExists() {
        givenUserFound();

        requestReset();

        thenDeletedPreviousSavedAndSentEmail();
    }

    @Test
    @DisplayName("Envía el email a la dirección del usuario")
    void executeShouldSendEmailToCorrectAddress() {
        givenUserFound();

        requestReset();

        thenEmailSentTo(EMAIL);
    }

    @Test
    @DisplayName("Guarda el token con el userId correcto y una expiración posterior a la creación")
    void executeShouldSaveTokenWithCorrectUserId() {
        givenUserFound();

        requestReset();

        thenSavedTokenHasCorrectUserId();
    }

    @Test
    @DisplayName("Borra los tokens previos antes de guardar el nuevo")
    void executeShouldDeletePreviousTokensBeforeSavingNew() {
        givenUserFound();

        requestReset();

        thenDeletedBeforeSaving();
    }

    @Test
    @DisplayName("Genera un token de 6 caracteres con el alfabeto permitido")
    void executeShouldGenerateTokenWithValidFormat() {
        givenUserFound();

        requestReset();

        thenTokenHasValidFormat();
    }

    @Test
    @DisplayName("Lanza ResourceNotFound cuando el email no existe")
    void executeShouldThrowResourceNotFoundWhenEmailDoesNotExist() {
        givenUserNotFound();

        thenRequestResetThrowsResourceNotFound();
    }

    @Test
    @DisplayName("No envía email ni guarda token cuando el email no existe")
    void executeShouldNeverSendEmailWhenEmailDoesNotExist() {
        givenUserNotFound();

        thenRequestResetThrowsResourceNotFound();
        thenNeverSentEmailNorSavedToken();
    }

    // --- arrange ---

    private void givenUserFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
    }

    private void givenUserNotFound() {
        when(userRepository.findByEmail(MISSING_EMAIL)).thenReturn(Optional.empty());
    }

    private AppUser buildUser() {
        return AppUser.builder()
                .id(USER_ID).email(EMAIL).password("encodedPass")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    // --- act ---

    private void requestReset() {
        useCase.execute(EMAIL);
    }

    // --- assert ---

    private void thenDeletedPreviousSavedAndSentEmail() {
        verify(passwordResetTokenRepository).deleteAllByUserId(USER_ID);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailPort).sendPasswordReset(any(), any());
    }

    private void thenEmailSentTo(String expectedAddress) {
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).sendPasswordReset(toCaptor.capture(), any());
        assertThat(toCaptor.getValue()).isEqualTo(expectedAddress);
    }

    private void thenSavedTokenHasCorrectUserId() {
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getToken()).isNotBlank();
        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());
    }

    private void thenDeletedBeforeSaving() {
        InOrder inOrder = inOrder(passwordResetTokenRepository);
        inOrder.verify(passwordResetTokenRepository).deleteAllByUserId(USER_ID);
        inOrder.verify(passwordResetTokenRepository).save(any());
    }

    private void thenTokenHasValidFormat() {
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        String token = tokenCaptor.getValue().getToken();
        assertThat(token).hasSize(6);
        assertThat(token).matches("[ABCDEFGHJKMNPQRSTUVWXYZ23456789]+");
    }

    private void thenRequestResetThrowsResourceNotFound() {
        assertThatThrownBy(() -> useCase.execute(MISSING_EMAIL))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenNeverSentEmailNorSavedToken() {
        verify(emailPort, never()).sendPasswordReset(any(), any());
        verify(passwordResetTokenRepository, never()).save(any());
    }
}
