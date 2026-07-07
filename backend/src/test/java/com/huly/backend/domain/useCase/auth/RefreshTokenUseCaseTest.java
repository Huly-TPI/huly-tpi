package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private static final String EMAIL = "user@huly.com";
    private static final String VALID_TOKEN = "validRefreshToken";

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenPort tokenPort;

    @InjectMocks private RefreshTokenUseCase refreshTokenUseCase;

    private AppUser activeUser;
    private RefreshToken storedToken;

    @BeforeEach
    void setUp() {
        activeUser = AppUser.builder()
                .id(1L).email(EMAIL)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();

        storedToken = RefreshToken.builder()
                .id(10L).userId(1L).token(VALID_TOKEN)
                .createdAt(Instant.now()).expiredAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    @DisplayName("Rota el token y devuelve un par nuevo cuando el token es válido")
    void executeShouldRotateTokenAndReturnNewPairWhenTokenIsValid() {
        givenTokenPassesJwtChecks(VALID_TOKEN);
        givenStoredTokenFound(VALID_TOKEN, storedToken);
        givenExtractedUserId(VALID_TOKEN, 1L);
        givenUserFound(1L, activeUser);
        givenNewTokensGeneratedWithExactData();

        AuthTokens result = refresh(VALID_TOKEN);

        thenRotatedTokens(result);
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el JWT no es válido")
    void executeShouldThrowInvalidCredentialsWhenJwtIsInvalid() {
        givenJwtInvalid("badToken");

        thenRefreshThrowsInvalidCredentials("badToken", "Invalid or expired refresh token");
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el token no es de tipo refresh")
    void executeShouldThrowInvalidCredentialsWhenTokenIsNotRefreshType() {
        givenNotRefreshType("accessToken");

        thenRefreshThrowsInvalidCredentials("accessToken", "Invalid or expired refresh token");
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el token no está en la base de datos")
    void executeShouldThrowInvalidCredentialsWhenTokenNotFoundInDb() {
        givenTokenPassesJwtChecks("orphanToken");
        givenStoredTokenNotFound("orphanToken");

        thenRefreshThrowsInvalidCredentials("orphanToken", "not found or already used");
    }

    @Test
    @DisplayName("Lanza InvalidCredentials y borra el token cuando el token almacenado expiró")
    void executeShouldThrowInvalidCredentialsAndDeleteTokenWhenStoredTokenIsExpired() {
        RefreshToken expired = expiredToken();
        givenTokenPassesJwtChecks("expiredToken");
        givenStoredTokenFound("expiredToken", expired);

        thenRefreshThrowsInvalidCredentials("expiredToken", "expired");
        thenDeletedStoredToken(expired);
        thenDidNotSaveNewToken();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials y borra el token cuando el token almacenado no tiene expiración")
    void executeShouldThrowInvalidCredentialsAndDeleteTokenWhenStoredTokenHasNoExpiration() {
        RefreshToken withoutExpiration = tokenWithoutExpiration();
        givenTokenPassesJwtChecks("nullExpiryToken");
        givenStoredTokenFound("nullExpiryToken", withoutExpiration);

        thenRefreshThrowsInvalidCredentials("nullExpiryToken", "expired");
        thenDeletedStoredToken(withoutExpiration);
        thenDidNotSaveNewToken();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el token no contiene el userId")
    void executeShouldThrowInvalidCredentialsWhenUserIdMissingFromToken() {
        givenTokenPassesJwtChecks("noUserIdToken");
        givenStoredTokenFound("noUserIdToken", storedToken);
        givenExtractedUserId("noUserIdToken", null);

        thenRefreshThrowsInvalidCredentials("noUserIdToken", "Invalid refresh token");
    }

    @Test
    @DisplayName("Lanza AccountNotActive cuando el usuario no existe")
    void executeShouldThrowAccountNotActiveWhenUserNotFound() {
        givenTokenPassesJwtChecks(VALID_TOKEN);
        givenStoredTokenFound(VALID_TOKEN, storedToken);
        givenExtractedUserId(VALID_TOKEN, 999L);
        givenUserNotFound(999L);

        thenRefreshThrowsAccountNotActive(VALID_TOKEN, "User not found");
    }

    @Test
    @DisplayName("Lanza AccountNotActive cuando el usuario no está activo")
    void executeShouldThrowAccountNotActiveWhenUserIsNotActive() {
        givenTokenPassesJwtChecks(VALID_TOKEN);
        givenStoredTokenFound(VALID_TOKEN, storedToken);
        givenExtractedUserId(VALID_TOKEN, 1L);
        givenUserFound(1L, blockedUser());

        thenRefreshThrowsAccountNotActive(VALID_TOKEN, "not active");
    }

    @Test
    @DisplayName("Guarda el nuevo refresh token con el userId y una expiración posterior a la creación")
    void executeShouldSaveNewRefreshTokenWithCorrectData() {
        givenTokenPassesJwtChecks(VALID_TOKEN);
        givenStoredTokenFound(VALID_TOKEN, storedToken);
        givenExtractedUserId(VALID_TOKEN, 1L);
        givenUserFound(1L, activeUser);
        givenNewTokensGeneratedWithAnyArgs();

        refresh(VALID_TOKEN);

        thenSavedNewRefreshTokenHasCorrectData();
    }

    // --- arrange ---

    private void givenTokenPassesJwtChecks(String token) {
        when(tokenPort.isTokenValid(token)).thenReturn(true);
        when(tokenPort.isRefreshToken(token)).thenReturn(true);
    }

    private void givenJwtInvalid(String token) {
        when(tokenPort.isTokenValid(token)).thenReturn(false);
    }

    private void givenNotRefreshType(String token) {
        when(tokenPort.isTokenValid(token)).thenReturn(true);
        when(tokenPort.isRefreshToken(token)).thenReturn(false);
    }

    private void givenStoredTokenFound(String token, RefreshToken stored) {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(stored));
    }

    private void givenStoredTokenNotFound(String token) {
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());
    }

    private void givenExtractedUserId(String token, Long userId) {
        when(tokenPort.extractUserId(token)).thenReturn(userId);
    }

    private void givenUserFound(Long userId, AppUser user) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void givenUserNotFound(Long userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
    }

    private void givenNewTokensGeneratedWithExactData() {
        when(tokenPort.generateAccessToken(1L, EMAIL, UserRole.USER, UserStatus.ACTIVE)).thenReturn("newAccess");
        when(tokenPort.generateRefreshToken(1L, EMAIL)).thenReturn("newRefresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
    }

    private void givenNewTokensGeneratedWithAnyArgs() {
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("newAccess");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("newRefresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
    }

    private RefreshToken expiredToken() {
        return RefreshToken.builder()
                .id(11L).userId(1L).token("expiredToken")
                .createdAt(Instant.now().minusSeconds(7200))
                .expiredAt(Instant.now().minusSeconds(60))
                .build();
    }

    private RefreshToken tokenWithoutExpiration() {
        // Sin expiración (expiredAt null): la política lo considera expirado.
        return RefreshToken.builder()
                .id(12L).userId(1L).token("nullExpiryToken")
                .createdAt(Instant.now().minusSeconds(60))
                .build();
    }

    private AppUser blockedUser() {
        return AppUser.builder()
                .id(1L).email(EMAIL)
                .role(UserRole.USER).status(UserStatus.BLOCKED)
                .build();
    }

    // --- act ---

    private AuthTokens refresh(String token) {
        return refreshTokenUseCase.execute(token);
    }

    // --- assert ---

    private void thenRotatedTokens(AuthTokens result) {
        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(result.getRefreshToken()).isEqualTo("newRefresh");
        verify(refreshTokenRepository).delete(storedToken);
    }

    private void thenRefreshThrowsInvalidCredentials(String token, String message) {
        assertThatThrownBy(() -> refreshTokenUseCase.execute(token))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining(message);
    }

    private void thenRefreshThrowsAccountNotActive(String token, String message) {
        assertThatThrownBy(() -> refreshTokenUseCase.execute(token))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining(message);
    }

    private void thenDeletedStoredToken(RefreshToken token) {
        verify(refreshTokenRepository).delete(token);
    }

    private void thenDidNotSaveNewToken() {
        verify(refreshTokenRepository, never()).save(any());
    }

    private void thenSavedNewRefreshTokenHasCorrectData() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertThat(saved.getToken()).isEqualTo("newRefresh");
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getExpiredAt()).isAfter(saved.getCreatedAt());
    }
}
