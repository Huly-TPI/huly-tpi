package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
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

    private static final String EMAIL = "admin@huly.com";
    private static final String PASSWORD = "password123";

    @Mock private UserRepository userRepository;
    @Mock private LoginUseCase loginUseCase;

    @InjectMocks private AdminLoginUseCase adminLoginUseCase;

    @Test
    @DisplayName("Devuelve los tokens del login cuando el usuario es ADMIN")
    void executeShouldReturnTokensWhenUserIsAdmin() {
        AuthTokens expected = adminTokens();
        givenUserFound(UserRole.ADMIN);
        givenLoginReturns(expected);

        AuthTokens result = adminLogin();

        thenResultIsSameAs(result, expected);
    }

    @Test
    @DisplayName("Delega en LoginUseCase cuando el usuario es ADMIN")
    void executeShouldDelegateToLoginUseCaseWhenUserIsAdmin() {
        givenUserFound(UserRole.ADMIN);
        givenLoginReturns(adminTokens());

        adminLogin();

        thenDelegatedToLogin();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el usuario no existe")
    void executeShouldThrowInvalidCredentialsWhenUserNotFound() {
        givenUserNotFound();

        thenAdminLoginThrowsInvalidCredentials();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el usuario tiene rol USER")
    void executeShouldThrowInvalidCredentialsWhenUserIsNotAdmin() {
        givenUserFound(UserRole.USER);

        thenAdminLoginThrowsInvalidCredentials();
    }

    @Test
    @DisplayName("Lanza InvalidCredentials cuando el usuario tiene rol LEAD")
    void executeShouldThrowInvalidCredentialsWhenUserIsLead() {
        givenUserFound(UserRole.LEAD);

        thenAdminLoginThrowsInvalidCredentials();
    }

    // --- arrange ---

    private void givenUserFound(UserRole role) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(role)));
    }

    private void givenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    }

    private void givenLoginReturns(AuthTokens tokens) {
        when(loginUseCase.execute(EMAIL, PASSWORD)).thenReturn(tokens);
    }

    private AppUser userWithRole(UserRole role) {
        return AppUser.builder()
                .id(1L).email(EMAIL).password("hashed")
                .role(role).status(UserStatus.ACTIVE)
                .build();
    }

    private AuthTokens adminTokens() {
        return AuthTokens.builder()
                .accessToken("token").refreshToken("refresh").role(UserRole.ADMIN)
                .build();
    }

    // --- act ---

    private AuthTokens adminLogin() {
        return adminLoginUseCase.execute(EMAIL, PASSWORD);
    }

    // --- assert ---

    private void thenResultIsSameAs(AuthTokens result, AuthTokens expected) {
        assertThat(result).isSameAs(expected);
    }

    private void thenDelegatedToLogin() {
        verify(loginUseCase).execute(EMAIL, PASSWORD);
    }

    private void thenAdminLoginThrowsInvalidCredentials() {
        assertThatThrownBy(this::adminLogin)
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
