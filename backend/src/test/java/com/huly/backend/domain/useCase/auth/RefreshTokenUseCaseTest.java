package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenProvider tokenProvider;

    @InjectMocks private RefreshTokenUseCase refreshTokenUseCase;

    private AppUser activeUser;
    private RefreshToken storedToken;

    @BeforeEach
    void setUp() {
        activeUser = AppUser.builder()
                .id(1L).email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();

        storedToken = RefreshToken.builder()
                .id(10L).userId(1L).token("validRefreshToken")
                .createdAt(Instant.now()).expiredAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void execute_shouldRotateTokenAndReturnNewPair_whenTokenIsValid() {
        when(tokenProvider.isTokenValid("validRefreshToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.extractEmail("validRefreshToken")).thenReturn("user@huly.com");
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(tokenProvider.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("newAccess");
        when(tokenProvider.generateRefreshToken(1L, "user@huly.com")).thenReturn("newRefresh");
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthTokens result = refreshTokenUseCase.execute("validRefreshToken");

        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(result.getRefreshToken()).isEqualTo("newRefresh");
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenJwtIsInvalid() {
        when(tokenProvider.isTokenValid("badToken")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenUseCase.execute("badToken"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenTokenNotFoundInDb() {
        when(tokenProvider.isTokenValid("orphanToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("orphanToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute("orphanToken"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not found or already used");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserNotFound() {
        when(tokenProvider.isTokenValid("validRefreshToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.extractEmail("validRefreshToken")).thenReturn("ghost@huly.com");
        when(userRepository.findByEmail("ghost@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute("validRefreshToken"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsNotActive() {
        AppUser inactiveUser = AppUser.builder()
                .id(1L).email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.BLOCKED)
                .build();

        when(tokenProvider.isTokenValid("validRefreshToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.extractEmail("validRefreshToken")).thenReturn("user@huly.com");
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> refreshTokenUseCase.execute("validRefreshToken"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void execute_shouldSaveNewRefreshTokenWithCorrectData() {
        when(tokenProvider.isTokenValid("validRefreshToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenProvider.extractEmail("validRefreshToken")).thenReturn("user@huly.com");
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(tokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("newAccess");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("newRefresh");
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenReturn(null);

        refreshTokenUseCase.execute("validRefreshToken");

        RefreshToken saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("newRefresh");
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getExpiredAt()).isAfter(saved.getCreatedAt());
    }
}
