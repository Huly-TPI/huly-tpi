package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.useCase.auth.LoginUseCase;
import com.huly.backend.domain.useCase.auth.LogoutUseCase;
import com.huly.backend.domain.useCase.auth.RefreshTokenUseCase;
import com.huly.backend.domain.useCase.auth.RegisterUseCase;
import com.huly.backend.exception.GlobalExceptionHandler;
import com.huly.backend.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LoginUseCase loginUseCase;
    private RegisterUseCase registerUseCase;
    private RefreshTokenUseCase refreshTokenUseCase;
    private LogoutUseCase logoutUseCase;
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        loginUseCase = mock(LoginUseCase.class);
        registerUseCase = mock(RegisterUseCase.class);
        refreshTokenUseCase = mock(RefreshTokenUseCase.class);
        logoutUseCase = mock(LogoutUseCase.class);
        tokenProvider = mock(TokenProvider.class);

        AuthController controller = new AuthController(
                loginUseCase, registerUseCase, refreshTokenUseCase, logoutUseCase, tokenProvider);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        when(tokenProvider.isCookieSecure()).thenReturn(false);
        when(tokenProvider.getRefreshTokenMaxAgeSecs()).thenReturn(604800L);
    }

    @Test
    void login_shouldReturn200WithAccessTokenRoleAndRefreshCookie() throws Exception {
        AuthTokens tokens = AuthTokens.builder()
                .accessToken("theAccessToken").refreshToken("theRefreshToken")
                .role(UserRole.USER).build();
        when(loginUseCase.execute("user@huly.com", "password123")).thenReturn(tokens);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "user@huly.com", "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("theAccessToken"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=theRefreshToken")));
    }

    @Test
    void login_shouldReturn400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "not-an-email", "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "user@huly.com", "password", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Juan", "email", "new@huly.com", "password", "password123","birthDate", "2000-01-01"))))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuario registrado correctamente"));

        verify(registerUseCase).execute("new@huly.com", "password123", "Juan");
    }

    @Test
    void register_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "", "email", "new@huly.com", "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Juan", "email", "bad-email", "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Juan", "email", "user@huly.com", "password", "abc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturn200WithNewTokensAndCookie_whenCookieIsPresent() throws Exception {
        AuthTokens tokens = AuthTokens.builder()
                .accessToken("newAccessToken").refreshToken("newRefreshToken").build();
        when(refreshTokenUseCase.execute("oldRefreshToken")).thenReturn(tokens);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "oldRefreshToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=newRefreshToken")));
    }

    @Test
    void refresh_shouldReturn401_whenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204WithClearedCookie_whenCookieIsPresent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "someToken")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(logoutUseCase).execute("someToken");
    }

    @Test
    void logout_shouldReturn204_whenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute(null);
    }
}
