package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminLoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginUseCase loginUseCase;

    @InjectMocks private AdminLoginUseCase adminLoginUseCase;

    private static final String EMAIL = "admin@huly.com";
    private static final String PASSWORD = "password123";

    private AppUser userWithRole(UserRole role) {
        return AppUser.builder()
                .id(1L).email(EMAIL).password("hashed")
                .role(role).status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_shouldReturnTokens_whenUserIsAdmin() {
        AuthTokens expected = AuthTokens.builder()
                .accessToken("token").refreshToken("refresh").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(UserRole.ADMIN)));
        when(loginUseCase.execute(EMAIL, PASSWORD)).thenReturn(expected);

        AuthTokens result = adminLoginUseCase.execute(EMAIL, PASSWORD);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void execute_shouldDelegateToLoginUseCase_whenUserIsAdmin() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(UserRole.ADMIN)));
        when(loginUseCase.execute(EMAIL, PASSWORD)).thenReturn(
                AuthTokens.builder().accessToken("t").refreshToken("r").role(UserRole.ADMIN).build());

        adminLoginUseCase.execute(EMAIL, PASSWORD);

        verify(loginUseCase).execute(EMAIL, PASSWORD);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminLoginUseCase.execute(EMAIL, PASSWORD))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsNotAdmin() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(UserRole.USER)));

        assertThatThrownBy(() -> adminLoginUseCase.execute(EMAIL, PASSWORD))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserIsLead() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(UserRole.LEAD)));

        assertThatThrownBy(() -> adminLoginUseCase.execute(EMAIL, PASSWORD))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }
}
