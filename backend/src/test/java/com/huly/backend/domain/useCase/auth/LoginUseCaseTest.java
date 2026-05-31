package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenProvider tokenProvider;
    @Mock private PasswordHasher passwordHasher;

    @InjectMocks private LoginUseCase loginUseCase;

    private AppUser activeUser;

    @BeforeEach
    void setUp() {
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
        when(passwordHasher.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenProvider.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(1L, "user@huly.com")).thenReturn("refreshToken");
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthTokens result = loginUseCase.execute("user@huly.com", "rawPass");

        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenEmailNotFound() {
        when(userRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute("missing@huly.com", "rawPass"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenPasswordDoesNotMatch() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasher.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute("user@huly.com", "wrongPass"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsNotActive() {
        AppUser inactiveUser = AppUser.builder()
                .id(2L).email("user@huly.com").password("encodedPass")
                .role(UserRole.USER).status(UserStatus.INACTIVE).build();
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordHasher.matches("rawPass", "encodedPass")).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.execute("user@huly.com", "rawPass"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void execute_shouldSaveRefreshTokenWithCorrectUserIdAndExpiration() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(activeUser));
        when(passwordHasher.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(), any())).thenReturn("refresh");
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);

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
        when(passwordHasher.matches("rawPass", "encodedPass")).thenReturn(true);
        when(tokenProvider.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE)).thenReturn("at");
        when(tokenProvider.generateRefreshToken(1L, "user@huly.com")).thenReturn("rt");
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(3600L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        loginUseCase.execute("user@huly.com", "rawPass");

        verify(tokenProvider).generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE);
        verify(tokenProvider).generateRefreshToken(1L, "user@huly.com");
    }
}
