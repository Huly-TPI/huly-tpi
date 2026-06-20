package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.payment.CoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-19T12:00:00Z");
    private static final int THRESHOLD_DAYS = 5;
    private static final int REWARD_COINS = 30;

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenPort tokenPort;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;
    @Mock private CoinService coinService;

    private LoginUseCase loginUseCase;

    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        loginUseCase = new LoginUseCase(userRepository, refreshTokenRepository, tokenPort, passwordHasherPort,
                userDetailDomainRepository, coinService, fixedClock, THRESHOLD_DAYS, REWARD_COINS);

        activeUser = AppUser.builder()
                .id(1L)
                .email("user@huly.com")
                .password("encodedPass")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_shouldReturnAuthTokensWithRoleAndTokens_whenCredentialsAreValid() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenPort.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("accessToken");
        when(tokenPort.generateRefreshToken(1L, "user@huly.com")).thenReturn("refreshToken");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");

        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenEmailNotFound() {
        when(userRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute("missing@huly.com", "rawPass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenPasswordDoesNotMatch() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute("user@huly.com", "wrongPass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsNotActive() {
        AppUser inactiveUser = AppUser.builder()
                .id(2L).email("user@huly.com").password("encodedPass")
                .role(UserRole.USER).status(UserStatus.INACTIVE).build();
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.execute("user@huly.com", "rawPass"))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void execute_shouldSaveRefreshTokenWithCorrectUserIdAndExpiration() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("refresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenReturn(null);

        loginUseCase.execute("user@huly.com", "rawPass");

        RefreshToken saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getToken()).isEqualTo("refresh");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getExpiredAt()).isAfter(saved.getCreatedAt());
    }

    @Test
    void execute_shouldCallGenerateTokensWithCorrectUserData() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenPort.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("at");
        when(tokenPort.generateRefreshToken(1L, "user@huly.com")).thenReturn("rt");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(3600L);

        loginUseCase.execute("user@huly.com", "rawPass");

        verify(tokenPort).generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE);
        verify(tokenPort).generateRefreshToken(1L, "user@huly.com");
    }

    @Test
    void execute_shouldReturnOnBoardingCompletedFromUserDetailDomainRepository() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("refresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.of(true));

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");
        assertThat(result.getOnBoardingCompleted()).isTrue();
    }

    @Test
    void execute_shouldReturnFalse_whenUserDetailNotFound() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasherPort.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("refresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.empty());

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");
        assertThat(result.getOnBoardingCompleted()).isFalse();
    }

    // --- Recompensa de retorno por inactividad ---

    private void stubValidLogin(AppUser user) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("rawPass", user.getPassword())).thenReturn(true);
        when(tokenPort.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(tokenPort.generateRefreshToken(any(), any())).thenReturn("refresh");
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
    }

    @Test
    void execute_shouldNotReward_whenFirstLogin() {
        stubValidLogin(activeUser);
        when(userDetailDomainRepository.findLastLoginDate(1L)).thenReturn(Optional.empty());

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");

        assertThat(result.getComebackReward()).isNull();
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository).updateLastLoginDate(1L, NOW);
    }

    @Test
    void execute_shouldNotReward_whenGapBelowThreshold() {
        stubValidLogin(activeUser);
        when(userDetailDomainRepository.findLastLoginDate(1L))
                .thenReturn(Optional.of(NOW.minus(Duration.ofDays(4))));

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");

        assertThat(result.getComebackReward()).isNull();
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository).updateLastLoginDate(1L, NOW);
    }

    @Test
    void execute_shouldReward_whenGapAtOrAboveThresholdAndRoleUser() {
        stubValidLogin(activeUser);
        when(userDetailDomainRepository.findLastLoginDate(1L))
                .thenReturn(Optional.of(NOW.minus(Duration.ofDays(5))));

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");

        assertThat(result.getComebackReward()).isEqualTo(REWARD_COINS);
        verify(coinService).credit(1L, REWARD_COINS);
        verify(userDetailDomainRepository).updateLastLoginDate(1L, NOW);
    }

    @Test
    void execute_shouldNotReward_whenRoleIsNotUser() {
        AppUser admin = AppUser.builder()
                .id(9L).email("admin@huly.com").password("encodedPass")
                .role(UserRole.ADMIN).status(UserStatus.ACTIVE).build();
        stubValidLogin(admin);

        AuthTokens result = loginUseCase.execute("admin@huly.com", "rawPass");

        assertThat(result.getComebackReward()).isNull();
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository, never()).findLastLoginDate(anyLong());
        verify(userDetailDomainRepository).updateLastLoginDate(9L, NOW);
    }
}
