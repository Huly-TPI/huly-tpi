package com.huly.backend.domain.service;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import com.huly.backend.infrastructure.security.JwtService;
import com.huly.backend.presentation.dto.auth.LoginRequest;
import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.presentation.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        AppUserEntity user = AppUserEntity.builder()
                .id(1L)
                .email("user@huly.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        LoginRequest request = new LoginRequest();
        request.setEmail("user@huly.com");
        request.setPassword("raw-password");

        when(appUserRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L, "user@huly.com")).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(jwtService).generateAccessToken(1L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE);
        verify(jwtService).generateRefreshToken(1L, "user@huly.com");
    }

    @Test
    void login_shouldThrowWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@huly.com");
        request.setPassword("raw-password");

        when(appUserRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_shouldThrowWhenPasswordDoesNotMatch() {
        AppUserEntity user = AppUserEntity.builder()
                .id(1L)
                .email("user@huly.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        LoginRequest request = new LoginRequest();
        request.setEmail("user@huly.com");
        request.setPassword("wrong-password");

        when(appUserRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void register_shouldCreateUserDetailAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@huly.com");
        request.setPassword("raw-password");
        request.setName("Mili");
        request.setBirthDate(LocalDate.of(2000, 1, 15));

        AppUserEntity savedUser = AppUserEntity.builder()
                .id(10L)
                .email("user@huly.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(appUserRepository.existsByEmail("user@huly.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUserEntity.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(10L, "user@huly.com", UserRole.USER, UserStatus.ACTIVE))
                .thenReturn("access-token");
        when(jwtService.generateRefreshToken(10L, "user@huly.com")).thenReturn("refresh-token");

        LoginResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<UserDetailEntity> userDetailCaptor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(userDetailCaptor.capture());
        UserDetailEntity savedUserDetail = userDetailCaptor.getValue();
        assertThat(savedUserDetail.getAppUser()).isSameAs(savedUser);
        assertThat(savedUserDetail.getName()).isEqualTo("Mili");
        assertThat(savedUserDetail.getBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
        assertThat(savedUserDetail.getOnboardingTutorialCompleted()).isFalse();
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@huly.com");
        request.setPassword("raw-password");
        request.setName("Mili");
        request.setBirthDate(LocalDate.of(2000, 1, 15));

        when(appUserRepository.existsByEmail("user@huly.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El email ya esta registrado");

        verifyNoInteractions(passwordEncoder, jwtService, userDetailRepository);
    }
}