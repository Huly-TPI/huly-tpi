package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private LogoutUseCase logoutUseCase;

    @Test
    void execute_shouldDeleteToken_whenTokenFoundInDb() {
        RefreshToken stored = RefreshToken.builder().id(1L).token("validToken").build();
        when(refreshTokenRepository.findByToken("validToken")).thenReturn(Optional.of(stored));

        logoutUseCase.execute("validToken");

        verify(refreshTokenRepository).delete(stored);
    }

    @Test
    void execute_shouldDoNothing_whenTokenNotFoundInDb() {
        when(refreshTokenRepository.findByToken("unknownToken")).thenReturn(Optional.empty());

        logoutUseCase.execute("unknownToken");

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void execute_shouldDoNothing_whenTokenIsNull() {
        logoutUseCase.execute(null);

        verifyNoInteractions(refreshTokenRepository);
    }
}
