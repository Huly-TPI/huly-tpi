package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.useCase.auth.*;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String requestBody;

    private LoginUseCase loginUseCase;
    private AdminLoginUseCase adminLoginUseCase;
    private RegisterUseCase registerUseCase;
    private RefreshTokenUseCase refreshTokenUseCase;
    private LogoutUseCase logoutUseCase;
    private TokenPort tokenPort;
    private RequestPasswordResetUseCase requestPasswordResetUseCase;
    private ResetPasswordUseCase resetPasswordUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = mock(LoginUseCase.class);
        adminLoginUseCase = mock(AdminLoginUseCase.class);
        registerUseCase = mock(RegisterUseCase.class);
        refreshTokenUseCase = mock(RefreshTokenUseCase.class);
        logoutUseCase = mock(LogoutUseCase.class);
        tokenPort = mock(TokenPort.class);
        requestPasswordResetUseCase = mock(RequestPasswordResetUseCase.class);
        resetPasswordUseCase = mock(ResetPasswordUseCase.class);

        AuthController controller = new AuthController(
                loginUseCase, adminLoginUseCase, registerUseCase, refreshTokenUseCase, logoutUseCase, tokenPort,
                requestPasswordResetUseCase, resetPasswordUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        when(tokenPort.isCookieSecure()).thenReturn(false);
        when(tokenPort.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
    }

    @Test
    @DisplayName("Devuelve 200 con access token, rol y cookie de refresh al iniciar sesión")
    void loginShouldReturn200WithAccessTokenRoleAndRefreshCookie() throws Exception {
        // --- arrange ---
        givenLoginReturnsTokens("user@huly.com", "password123", "theAccessToken", "theRefreshToken", UserRole.USER);
        givenLoginRequestBody("user@huly.com", "password123");

        // --- act ---
        ResultActions result = performLogin();

        // --- assert ---
        thenOkWithTokenRoleAndRefreshCookie(result, "theAccessToken", "USER", "theRefreshToken");
    }

    @Test
    @DisplayName("Emite cookie Secure con SameSite=None cuando la cookie segura está habilitada")
    void loginShouldIssueSecureSameSiteNoneCookieWhenCookieSecureEnabled() throws Exception {
        // --- arrange ---
        givenSecureCookieEnabled();
        givenLoginReturnsTokens("user@huly.com", "password123", "theAccessToken", "theRefreshToken", UserRole.USER);
        givenLoginRequestBody("user@huly.com", "password123");

        // --- act ---
        ResultActions result = performLogin();

        // --- assert ---
        thenOkWithSecureSameSiteNoneCookie(result);
    }

    @Test
    @DisplayName("Devuelve 400 al iniciar sesión cuando el email es inválido")
    void loginShouldReturn400WhenEmailIsInvalid() throws Exception {
        // --- arrange ---
        givenLoginRequestBody("not-an-email", "password123");

        // --- act ---
        ResultActions result = performLogin();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al iniciar sesión cuando la contraseña está en blanco")
    void loginShouldReturn400WhenPasswordIsBlank() throws Exception {
        // --- arrange ---
        givenLoginRequestBody("user@huly.com", "");

        // --- act ---
        ResultActions result = performLogin();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 200 con access token y rol cuando las credenciales de admin son válidas")
    void backofficeLoginShouldReturn200WithAccessTokenAndRoleWhenAdminCredentialsAreValid() throws Exception {
        // --- arrange ---
        givenAdminLoginReturnsTokens("admin@huly.com", "password123", "adminAccessToken", "adminRefreshToken", UserRole.ADMIN);
        givenLoginRequestBody("admin@huly.com", "password123");

        // --- act ---
        ResultActions result = performBackofficeLogin();

        // --- assert ---
        thenOkWithTokenRoleAndRefreshCookie(result, "adminAccessToken", "ADMIN", "adminRefreshToken");
    }

    @Test
    @DisplayName("Devuelve 401 en el login de backoffice cuando las credenciales son inválidas")
    void backofficeLoginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        // --- arrange ---
        givenAdminLoginThrowsUnauthorized("admin@huly.com", "wrongpassword", "Invalid credentials");
        givenLoginRequestBody("admin@huly.com", "wrongpassword");

        // --- act ---
        ResultActions result = performBackofficeLogin();

        // --- assert ---
        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 201 con access token, rol y cookie de refresh al registrarse")
    void registerShouldReturn201WithAccessTokenRoleAndRefreshCookie() throws Exception {
        // --- arrange ---
        givenRegisterReturnsTokens("new@huly.com", "password123", "Juan", LocalDate.of(2000, 1, 1),
                "theAccessToken", "theRefreshToken", UserRole.USER);
        givenRegisterRequestBody("Juan", "new@huly.com", "password123", "2000-01-01");

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenCreatedWithTokenRoleAndRefreshCookie(result, "theAccessToken", "USER", "theRefreshToken");
        thenRegisterWasExecutedWith("new@huly.com", "password123", "Juan", LocalDate.of(2000, 1, 1));
    }

    @Test
    @DisplayName("Devuelve 400 al registrarse cuando el nombre está en blanco")
    void registerShouldReturn400WhenNameIsBlank() throws Exception {
        // --- arrange ---
        givenRegisterRequestBody("", "new@huly.com", "password123", null);

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al registrarse cuando el email es inválido")
    void registerShouldReturn400WhenEmailIsInvalid() throws Exception {
        // --- arrange ---
        givenRegisterRequestBody("Juan", "bad-email", "password123", null);

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al registrarse cuando la contraseña es demasiado corta")
    void registerShouldReturn400WhenPasswordIsTooShort() throws Exception {
        // --- arrange ---
        givenRegisterRequestBody("Juan", "user@huly.com", "abc", null);

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al registrarse cuando el nombre contiene números")
    void registerShouldReturn400WhenNameContainsNumbers() throws Exception {
        // --- arrange ---
        givenRegisterRequestBody("Juan123", "user@huly.com", "password123", "2000-01-01");

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenBadRequestWithNameError(result);
    }

    @Test
    @DisplayName("Devuelve 400 al registrarse cuando el nombre es solo un punto")
    void registerShouldReturn400WhenNameIsOnlyAPoint() throws Exception {
        // --- arrange ---
        givenRegisterRequestBody(".", "user@huly.com", "password123", "2000-01-01");

        // --- act ---
        ResultActions result = performRegister();

        // --- assert ---
        thenBadRequestWithNameError(result);
    }

    @Test
    @DisplayName("Devuelve 200 con nuevos tokens y cookie cuando la cookie de refresh está presente")
    void refreshShouldReturn200WithNewTokensAndCookieWhenCookieIsPresent() throws Exception {
        // --- arrange ---
        givenRefreshReturnsTokens("oldRefreshToken", "newAccessToken", "newRefreshToken");

        // --- act ---
        ResultActions result = performRefreshWithCookie("oldRefreshToken");

        // --- assert ---
        thenOkWithAccessTokenAndRefreshCookie(result, "newAccessToken", "newRefreshToken");
    }

    @Test
    @DisplayName("Devuelve 401 al refrescar cuando no hay cookie presente")
    void refreshShouldReturn401WhenNoCookiePresent() throws Exception {
        // --- act ---
        ResultActions result = performRefresh();

        // --- assert ---
        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 204 con la cookie limpiada al cerrar sesión cuando la cookie está presente")
    void logoutShouldReturn204WithClearedCookieWhenCookieIsPresent() throws Exception {
        // --- act ---
        ResultActions result = performLogoutWithCookie("someToken");

        // --- assert ---
        thenNoContentWithClearedCookie(result);
        thenLogoutWasExecutedWith("someToken");
    }

    @Test
    @DisplayName("Devuelve 204 al cerrar sesión cuando no hay cookie presente")
    void logoutShouldReturn204WhenNoCookiePresent() throws Exception {
        // --- act ---
        ResultActions result = performLogout();

        // --- assert ---
        thenNoContent(result);
        thenLogoutWasExecutedWith(null);
    }

    @Test
    @DisplayName("Devuelve 204 al solicitar recuperación de contraseña cuando el email existe")
    void forgotPasswordShouldReturn204WhenEmailExists() throws Exception {
        // --- arrange ---
        givenForgotPasswordRequestBody("user@huly.com");

        // --- act ---
        ResultActions result = performForgotPassword();

        // --- assert ---
        thenNoContent(result);
        thenPasswordResetRequestedFor("user@huly.com");
    }

    @Test
    @DisplayName("Devuelve 404 al solicitar recuperación de contraseña cuando el email no existe")
    void forgotPasswordShouldReturn404WhenEmailDoesNotExist() throws Exception {
        // --- arrange ---
        givenForgotPasswordThrowsNotFound("missing@huly.com", "No existe una cuenta con ese email");
        givenForgotPasswordRequestBody("missing@huly.com");

        // --- act ---
        ResultActions result = performForgotPassword();

        // --- assert ---
        thenNotFound(result);
    }

    @Test
    @DisplayName("Devuelve 400 al solicitar recuperación de contraseña cuando el email es inválido")
    void forgotPasswordShouldReturn400WhenEmailIsInvalid() throws Exception {
        // --- arrange ---
        givenForgotPasswordRequestBody("not-an-email");

        // --- act ---
        ResultActions result = performForgotPassword();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al solicitar recuperación de contraseña cuando el email está en blanco")
    void forgotPasswordShouldReturn400WhenEmailIsBlank() throws Exception {
        // --- arrange ---
        givenForgotPasswordRequestBody("");

        // --- act ---
        ResultActions result = performForgotPassword();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 204 al restablecer la contraseña cuando el token es válido")
    void resetPasswordShouldReturn204WhenTokenIsValid() throws Exception {
        // --- arrange ---
        givenResetPasswordRequestBody("valid-uuid-token", "newPass123");

        // --- act ---
        ResultActions result = performResetPassword();

        // --- assert ---
        thenNoContent(result);
        thenPasswordWasReset("valid-uuid-token", "newPass123");
    }

    @Test
    @DisplayName("Devuelve 404 al restablecer la contraseña cuando el token es inválido o expiró")
    void resetPasswordShouldReturn404WhenTokenIsInvalidOrExpired() throws Exception {
        // --- arrange ---
        givenResetPasswordThrowsNotFound("bad-token", "newPass123", "Token inválido o expirado");
        givenResetPasswordRequestBody("bad-token", "newPass123");

        // --- act ---
        ResultActions result = performResetPassword();

        // --- assert ---
        thenNotFound(result);
    }

    @Test
    @DisplayName("Devuelve 400 al restablecer la contraseña cuando el token está en blanco")
    void resetPasswordShouldReturn400WhenTokenIsBlank() throws Exception {
        // --- arrange ---
        givenResetPasswordRequestBody("", "newPass123");

        // --- act ---
        ResultActions result = performResetPassword();

        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 al restablecer la contraseña cuando la nueva contraseña es demasiado corta")
    void resetPasswordShouldReturn400WhenNewPasswordIsTooShort() throws Exception {
        // --- arrange ---
        givenResetPasswordRequestBody("valid-uuid-token", "abc");

        // --- act ---
        ResultActions result = performResetPassword();

        // --- assert ---
        thenBadRequest(result);
    }

    // --- arrange ---
    private void givenSecureCookieEnabled() {
        when(tokenPort.isCookieSecure()).thenReturn(true);
    }

    private void givenLoginReturnsTokens(String email, String password,
                                         String accessToken, String refreshToken, UserRole role) {
        when(loginUseCase.execute(email, password)).thenReturn(authTokens(accessToken, refreshToken, role));
    }

    private void givenAdminLoginReturnsTokens(String email, String password,
                                              String accessToken, String refreshToken, UserRole role) {
        when(adminLoginUseCase.execute(email, password)).thenReturn(authTokens(accessToken, refreshToken, role));
    }

    private void givenAdminLoginThrowsUnauthorized(String email, String password, String message) {
        when(adminLoginUseCase.execute(email, password)).thenThrow(new UnauthorizedException(message));
    }

    private void givenRegisterReturnsTokens(String email, String password, String name, LocalDate birthDate,
                                            String accessToken, String refreshToken, UserRole role) {
        when(registerUseCase.execute(email, password, name, birthDate))
                .thenReturn(authTokens(accessToken, refreshToken, role));
    }

    private void givenRefreshReturnsTokens(String oldToken, String accessToken, String refreshToken) {
        when(refreshTokenUseCase.execute(oldToken)).thenReturn(authTokens(accessToken, refreshToken, null));
    }

    private void givenForgotPasswordThrowsNotFound(String email, String message) {
        doThrow(new ResourceNotFoundException(message)).when(requestPasswordResetUseCase).execute(email);
    }

    private void givenResetPasswordThrowsNotFound(String token, String newPassword, String message) {
        doThrow(new ResourceNotFoundException(message)).when(resetPasswordUseCase).execute(token, newPassword);
    }

    private AuthTokens authTokens(String accessToken, String refreshToken, UserRole role) {
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }

    private void givenLoginRequestBody(String email, String password) throws Exception {
        requestBody = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private void givenRegisterRequestBody(String name, String email, String password, String birthDate) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        if (birthDate != null) {
            body.put("birthDate", birthDate);
        }
        requestBody = objectMapper.writeValueAsString(body);
    }

    private void givenForgotPasswordRequestBody(String email) throws Exception {
        requestBody = objectMapper.writeValueAsString(Map.of("email", email));
    }

    private void givenResetPasswordRequestBody(String token, String newPassword) throws Exception {
        requestBody = objectMapper.writeValueAsString(Map.of("token", token, "newPassword", newPassword));
    }

    // --- act ---
    private ResultActions performLogin() throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions performBackofficeLogin() throws Exception {
        return mockMvc.perform(post("/api/auth/backoffice/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions performRegister() throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions performRefreshWithCookie(String token) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", token)));
    }

    private ResultActions performRefresh() throws Exception {
        return mockMvc.perform(post("/api/auth/refresh"));
    }

    private ResultActions performLogoutWithCookie(String token) throws Exception {
        return mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("refreshToken", token)));
    }

    private ResultActions performLogout() throws Exception {
        return mockMvc.perform(post("/api/auth/logout"));
    }

    private ResultActions performForgotPassword() throws Exception {
        return mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions performResetPassword() throws Exception {
        return mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    // --- assert ---
    private void thenOkWithTokenRoleAndRefreshCookie(ResultActions result, String accessToken, String role,
                                                     String refreshToken) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.role").value(role))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=" + refreshToken)));
    }

    private void thenOkWithSecureSameSiteNoneCookie(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=None")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")));
    }

    private void thenCreatedWithTokenRoleAndRefreshCookie(ResultActions result, String accessToken, String role,
                                                         String refreshToken) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.role").value(role))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=" + refreshToken)));
    }

    private void thenOkWithAccessTokenAndRefreshCookie(ResultActions result, String accessToken,
                                                      String refreshToken) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=" + refreshToken)));
    }

    private void thenNoContentWithClearedCookie(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenBadRequestWithNameError(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name")
                        .value("El nombre debe tener al menos 3 letras y solo puede contener letras y espacios"));
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    private void thenRegisterWasExecutedWith(String email, String password, String name, LocalDate birthDate) {
        verify(registerUseCase).execute(email, password, name, birthDate);
    }

    private void thenLogoutWasExecutedWith(String token) {
        verify(logoutUseCase).execute(token);
    }

    private void thenPasswordResetRequestedFor(String email) {
        verify(requestPasswordResetUseCase).execute(email);
    }

    private void thenPasswordWasReset(String token, String newPassword) {
        verify(resetPasswordUseCase).execute(token, newPassword);
    }
}
