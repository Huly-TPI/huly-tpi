package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsRequest;
import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsResponse;
import com.huly.backend.domain.mapper.pushNotification.UnsubscribeFromEmailsMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnsubscribeFromEmailsUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final String VALID_TOKEN = "tok-123";
    private static final String INVALID_TOKEN = "bad";

    @Mock
    private UserRepository userRepository;

    private UnsubscribeFromEmailsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UnsubscribeFromEmailsUseCase(userRepository, new UnsubscribeFromEmailsMapper());
    }

    @Test
    @DisplayName("Da de baja los emails y devuelve éxito cuando el token es válido")
    void executeShouldDisableEmailsAndReturnTrueWhenTokenIsValid() {
        // --- arrange ---
        givenValidToken();
        // --- act ---
        UnsubscribeFromEmailsResponse result = unsubscribe(VALID_TOKEN);
        // --- assert ---
        thenEmailsDisabled(result);
    }

    @Test
    @DisplayName("Devuelve fallo y no da de baja cuando el token es inválido")
    void executeShouldReturnFalseWhenTokenIsInvalid() {
        // --- arrange ---
        givenInvalidToken();
        // --- act ---
        UnsubscribeFromEmailsResponse result = unsubscribe(INVALID_TOKEN);
        // --- assert ---
        thenNotDisabled(result);
    }

    // --- arrange ---

    private void givenValidToken() {
        when(userRepository.findByUnsubscribeToken(VALID_TOKEN))
                .thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
    }

    private void givenInvalidToken() {
        when(userRepository.findByUnsubscribeToken(INVALID_TOKEN)).thenReturn(Optional.empty());
    }

    // --- act ---

    private UnsubscribeFromEmailsResponse unsubscribe(String token) {
        return useCase.execute(new UnsubscribeFromEmailsRequest(token));
    }

    // --- assert ---

    private void thenEmailsDisabled(UnsubscribeFromEmailsResponse result) {
        assertThat(result.success()).isTrue();
        verify(userRepository).disableReengagementEmails(USER_ID);
    }

    private void thenNotDisabled(UnsubscribeFromEmailsResponse result) {
        assertThat(result.success()).isFalse();
        verify(userRepository, never()).disableReengagementEmails(anyLong());
    }
}
