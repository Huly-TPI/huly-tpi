package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    private static final String VALID_TOKEN = "validToken";
    private static final String UNKNOWN_TOKEN = "unknownToken";

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private LogoutUseCase logoutUseCase;

    @Test
    @DisplayName("Borra el token cuando existe en la base de datos")
    void executeShouldDeleteTokenWhenTokenFoundInDb() {
        RefreshToken stored = storedToken();
        givenTokenFound(stored);

        logout(VALID_TOKEN);

        thenDeletedToken(stored);
    }

    @Test
    @DisplayName("No borra nada cuando el token no existe en la base de datos")
    void executeShouldDoNothingWhenTokenNotFoundInDb() {
        givenTokenNotFound();

        logout(UNKNOWN_TOKEN);

        thenNothingDeleted();
    }

    @Test
    @DisplayName("No interactúa con el repositorio cuando el token es null")
    void executeShouldDoNothingWhenTokenIsNull() {
        logout(null);

        thenNoInteractions();
    }

    // --- arrange ---

    private void givenTokenFound(RefreshToken stored) {
        when(refreshTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(stored));
    }

    private void givenTokenNotFound() {
        when(refreshTokenRepository.findByToken(UNKNOWN_TOKEN)).thenReturn(Optional.empty());
    }

    private RefreshToken storedToken() {
        return RefreshToken.builder().id(1L).token(VALID_TOKEN).build();
    }

    // --- act ---

    private void logout(String rawToken) {
        logoutUseCase.execute(rawToken);
    }

    // --- assert ---

    private void thenDeletedToken(RefreshToken stored) {
        verify(refreshTokenRepository).delete(stored);
    }

    private void thenNothingDeleted() {
        verify(refreshTokenRepository, never()).delete(any());
    }

    private void thenNoInteractions() {
        verifyNoInteractions(refreshTokenRepository);
    }
}
