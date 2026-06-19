package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.repository.UserRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenPort tokenPort;

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
        mockValidRefreshToken("validRefreshToken");
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenPort.extractUserId("validRefreshToken")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(tokenPort.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("newAccess");
        when(tokenPort.generateRefreshToken(1L, "user@huly.com")).thenReturn("newRefresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

        AuthTokens result = refreshTokenUseCase.execute("validRefreshToken");

        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(result.getRefreshToken()).isEqualTo("newRefresh");
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenJwtIsInvalid() {
        when(tokenPort.isTokenValid("badToken")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenUseCase.execute("badToken"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenTokenIsNotRefreshType() {
        when(tokenPort.isTokenValid("accessToken")).thenReturn(true);
        when(tokenPort.isRefreshToken("accessToken")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenUseCase.execute("accessToken"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenTokenNotFoundInDb() {
        mockValidRefreshToken("orphanToken");
        when(refreshTokenRepository.findByToken("orphanToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute("orphanToken"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("not found or already used");
    }

    @Test
    void execute_shouldThrowUnauthorizedAndDeleteToken_whenStoredTokenIsExpired() {
        RefreshToken expired = RefreshToken.builder()
                .id(11L).userId(1L).token("expiredToken")
                .createdAt(Instant.now().minusSeconds(7200))
                .expiredAt(Instant.now().minusSeconds(60))
                .build();

        mockValidRefreshToken("expiredToken");
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenUseCase.execute("expiredToken"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIdMissingFromToken() {
        mockValidRefreshToken("noUserIdToken");
        when(refreshTokenRepository.findByToken("noUserIdToken")).thenReturn(Optional.of(storedToken));
        when(tokenPort.extractUserId("noUserIdToken")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenUseCase.execute("noUserIdToken"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserNotFound() {
        mockValidRefreshToken("validRefreshToken");
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenPort.extractUserId("validRefreshToken")).thenReturn(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute("validRefreshToken"))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsNotActive() {
        AppUser inactiveUser = AppUser.builder()
                .id(1L).email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.BLOCKED)
                .build();

        mockValidRefreshToken("validRefreshToken");
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenPort.extractUserId("validRefreshToken")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> refreshTokenUseCase.execute("validRefreshToken"))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void execute_shouldSaveNewRefreshTokenWithCorrectData() {
        mockValidRefreshToken("validRefreshToken");
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(Optional.of(storedToken));
        when(tokenPort.extractUserId("validRefreshToken")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("newAccess");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("newRefresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenReturn(null);

        refreshTokenUseCase.execute("validRefreshToken");

        RefreshToken saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("newRefresh");
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getExpiredAt()).isAfter(saved.getCreatedAt());
    }

    private void mockValidRefreshToken(String token) {
        when(tokenPort.isTokenValid(token)).thenReturn(true);
        when(tokenPort.isRefreshToken(token)).thenReturn(true);
    }
}