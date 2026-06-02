package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.useCase.auth.AdminLoginUseCase;
import com.huly.backend.domain.useCase.auth.LoginUseCase;
import com.huly.backend.domain.useCase.auth.LogoutUseCase;
import com.huly.backend.domain.useCase.auth.RefreshTokenUseCase;
import com.huly.backend.domain.useCase.auth.RegisterUseCase;
import com.huly.backend.exception.UnauthorizedException;
import com.huly.backend.presentation.dto.auth.LoginRequest;
import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.presentation.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST de autenticación. Gestiona el ciclo completo de sesión:
 * registro, login (usuario y admin), renovación de tokens y logout.
 *
 * Estrategia de tokens:
 *  - accessToken: se devuelve en el body JSON (corta duración, usado en cada request).
 *  - refreshToken: se envía en una cookie HttpOnly (larga duración, no accesible desde JS).
 *
 * La cookie HttpOnly protege contra robo de tokens vía XSS, ya que el navegador
 * la envía automáticamente sin que el JS del frontend pueda leerla.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final AdminLoginUseCase adminLoginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final TokenProvider tokenProvider;

    /** Login para usuarios regulares. Devuelve accessToken en body y refreshToken en cookie. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokens tokens = loginUseCase.execute(request.getEmail(), request.getPassword());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(LoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .role(tokens.getRole())
                        .profileOnBoardingCompleted(tokens.getProfileOnBoardingCompleted())
                        .build());
    }

    /** Login exclusivo para administradores (backoffice). No incluye profileOnBoardingCompleted. */
    @PostMapping("/backoffice/login")
    public ResponseEntity<LoginResponse> backofficeLogin(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokens tokens = adminLoginUseCase.execute(request.getEmail(), request.getPassword());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(LoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .role(tokens.getRole())
                        .build());
    }

    /** Crea el usuario y hace login automático en el mismo request (201 Created). */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthTokens tokens = registerUseCase.execute(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getBirthDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(LoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .role(tokens.getRole())
                        .profileOnBoardingCompleted(tokens.getProfileOnBoardingCompleted())
                        .build());
    }

    /**
     * Renueva el accessToken usando el refreshToken de la cookie.
     * El refreshToken viejo se invalida y se emite uno nuevo (token rotation).
     * Si no viene cookie, lanza 401.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token null");
        }

        AuthTokens tokens = refreshTokenUseCase.execute(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.getRefreshToken()).toString())
                .body(LoginResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .build());
    }

    /**
     * Cierra sesión: invalida el refreshToken en BD y sobreescribe la cookie
     * con una vacía de maxAge=0 para que el navegador la elimine.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(required = false) String refreshToken
    ) {
        logoutUseCase.execute(refreshToken);

        // Cookie vacía con maxAge=0 → el navegador la borra inmediatamente
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(tokenProvider.isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    /**
     * Construye la cookie del refreshToken con las flags de seguridad correctas:
     * - httpOnly: JS no puede leerla (protección XSS)
     * - secure: solo se envía por HTTPS (configurable por entorno)
     * - sameSite=Lax: protección básica contra CSRF
     */
    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(tokenProvider.isCookieSecure())
                .path("/")
                .maxAge(tokenProvider.getRefreshTokenMaxAgeSecs())
                .sameSite("Lax")
                .build();
    }
}
