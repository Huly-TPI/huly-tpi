package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    private static final String EMAIL = "user@huly.com";
    private static final String MISSING_EMAIL = "missing@huly.com";
    private static final String RAW_PASSWORD = "rawPass";
    private static final String WRONG_PASSWORD = "wrongPass";
    private static final String ENCODED_PASSWORD = "encodedPass";
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenPort tokenPort;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;

    private LoginUseCase loginUseCase;
    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        loginUseCase = new LoginUseCase(userRepository, refreshTokenRepository, tokenPort,
                passwordHasherPort, userDetailDomainRepository, fixedClock);
        activeUser = AppUser.builder()
                .id(1L).email(EMAIL).password(ENCODED_PASSWORD)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Devuelve access token, refresh token y rol cuando las credenciales son válidas")
    void executeShouldReturnAuthTokensWhenCredentialsAreValid() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithExactData("accessToken", "refreshToken");

        AuthTokens result = login();

        thenReturnedTokens(result, "accessToken", "refreshToken", UserRole.USER);
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el email no existe")
    void executeShouldThrowInvalidCredentialsWhenEmailNotFound() {
        givenUserNotFoundByEmail();

        thenLoginThrowsInvalidCredentialsForMissingEmail();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando la contraseña no coincide")
    void executeShouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        givenUserFoundByEmail();
        givenWrongPassword();

        thenLoginThrowsInvalidCredentialsForWrongPassword();
    }

    @Test
    @DisplayName("Lanza AccountNotActive cuando el usuario no está activo")
    void executeShouldThrowAccountNotActiveWhenUserIsNotActive() {
        givenInactiveUserFoundByEmail();
        givenPasswordMatches();

        thenLoginThrowsAccountNotActive();
    }

    @Test
    @DisplayName("Guarda el refresh token con el userId y una expiración posterior a la creación")
    void executeShouldSaveRefreshTokenWithCorrectUserIdAndExpiration() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithAnyArgs("access", "refresh");

        login();

        thenSavedRefreshTokenHasCorrectData();
    }

    @Test
    @DisplayName("Genera los tokens con los datos del usuario autenticado")
    void executeShouldCallGenerateTokensWithCorrectUserData() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithExactData("at", "rt");

        login();

        thenGeneratedTokensWithUserData();
    }

    @Test
    @DisplayName("Devuelve onBoardingCompleted en true cuando el detalle del usuario lo indica")
    void executeShouldReturnOnBoardingCompletedFromUserDetail() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithAnyArgs("access", "refresh");
        givenOnBoardingCompleted();

        AuthTokens result = login();

        thenOnBoardingCompletedIsTrue(result);
    }

    @Test
    @DisplayName("Devuelve onBoardingCompleted en false cuando no hay detalle del usuario")
    void executeShouldReturnFalseWhenUserDetailNotFound() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithAnyArgs("access", "refresh");
        givenOnBoardingNotFound();

        AuthTokens result = login();

        thenOnBoardingCompletedIsFalse(result);
    }

    @Test
    @DisplayName("Registra la actividad de hoy cuando no hay un comeback pendiente")
    void executeShouldRegisterActivityWhenNoPendingComeback() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenLastLoginDate(TODAY.minusDays(2));

        login();

        thenRegisteredActivity();
    }

    @Test
    @DisplayName("Difiere la actividad cuando hay un comeback pendiente")
    void executeShouldDeferActivityWhenComebackPending() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenLastLoginDate(TODAY.minusDays(ComebackRewardPolicy.INACTIVE_DAYS_THRESHOLD));

        login();

        thenDeferredActivity();
    }

    @Test
    @DisplayName("Actualiza la fecha del último login cuando las credenciales son válidas")
    void executeShouldUpdateLastLoginWhenCredentialsAreValid() {
        givenUserFoundByEmail();
        givenPasswordMatches();
        givenTokensGeneratedWithAnyArgs("access", "refresh");

        login();

        thenUpdatedLastLogin();
    }

    // --- arrange ---

    private void givenUserFoundByEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
    }

    private void givenUserNotFoundByEmail() {
        when(userRepository.findByEmail(MISSING_EMAIL)).thenReturn(Optional.empty());
    }

    private void givenInactiveUserFoundByEmail() {
        AppUser inactiveUser = AppUser.builder()
                .id(1L).email(EMAIL).password(ENCODED_PASSWORD)
                .role(UserRole.USER).status(UserStatus.INACTIVE)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(inactiveUser));
    }

    private void givenPasswordMatches() {
        when(passwordHasherPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    }

    private void givenWrongPassword() {
        when(passwordHasherPort.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);
    }

    private void givenTokensGeneratedWithExactData(String access, String refresh) {
        when(tokenPort.generateAccessToken(1L, EMAIL, UserRole.USER, UserStatus.ACTIVE)).thenReturn(access);
        when(tokenPort.generateRefreshToken(1L, EMAIL)).thenReturn(refresh);
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenReturn(null);
    }

    private void givenTokensGeneratedWithAnyArgs(String access, String refresh) {
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn(access);
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn(refresh);
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenReturn(null);
    }

    private void givenOnBoardingCompleted() {
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.of(true));
    }

    private void givenOnBoardingNotFound() {
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.empty());
    }

    private void givenLastLoginDate(LocalDate date) {
        when(userDetailDomainRepository.findLastLoginDate(1L)).thenReturn(Optional.of(date));
    }

    // --- act ---

    private AuthTokens login() {
        return loginUseCase.execute(EMAIL, RAW_PASSWORD);
    }

    // --- assert ---

    private void thenReturnedTokens(AuthTokens result, String access, String refresh, UserRole role) {
        assertThat(result.getAccessToken()).isEqualTo(access);
        assertThat(result.getRefreshToken()).isEqualTo(refresh);
        assertThat(result.getRole()).isEqualTo(role);
    }

    private void thenLoginThrowsInvalidCredentialsForMissingEmail() {
        assertThatThrownBy(() -> loginUseCase.execute(MISSING_EMAIL, RAW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    private void thenLoginThrowsInvalidCredentialsForWrongPassword() {
        assertThatThrownBy(() -> loginUseCase.execute(EMAIL, WRONG_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    private void thenLoginThrowsAccountNotActive() {
        assertThatThrownBy(this::login)
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("not active");
    }

    private void thenSavedRefreshTokenHasCorrectData() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getToken()).isEqualTo("refresh");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getExpiredAt()).isAfter(saved.getCreatedAt());
    }

    private void thenGeneratedTokensWithUserData() {
        verify(tokenPort).generateAccessToken(1L, EMAIL, UserRole.USER, UserStatus.ACTIVE);
        verify(tokenPort).generateRefreshToken(1L, EMAIL);
    }

    private void thenOnBoardingCompletedIsTrue(AuthTokens result) {
        assertThat(result.getOnBoardingCompleted()).isTrue();
    }

    private void thenOnBoardingCompletedIsFalse(AuthTokens result) {
        assertThat(result.getOnBoardingCompleted()).isFalse();
    }

    private void thenRegisteredActivity() {
        verify(userDetailDomainRepository).updateLastLoginDate(1L, TODAY);
    }

    private void thenDeferredActivity() {
        verify(userDetailDomainRepository, never()).updateLastLoginDate(any(), any());
    }

    private void thenUpdatedLastLogin() {
        verify(userRepository).updateLastLogin(1L);
    }
}
