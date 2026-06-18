package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.useCase.auth.AdminLoginUseCase;
import com.huly.backend.domain.useCase.auth.LoginUseCase;
import com.huly.backend.domain.useCase.auth.LogoutUseCase;
import com.huly.backend.domain.useCase.auth.RefreshTokenUseCase;
import com.huly.backend.domain.useCase.auth.RegisterUseCase;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import com.huly.backend.infrastructure.presentation.dto.auth.LoginRequest;
import com.huly.backend.infrastructure.presentation.dto.auth.LoginResponse;
import com.huly.backend.infrastructure.presentation.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        .onBoardingCompleted(tokens.getOnBoardingCompleted())
                        .build());
    }
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
                    .onBoardingCompleted(tokens.getOnBoardingCompleted())
                    .build());

    }

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(required = false) String refreshToken
    ) {
        logoutUseCase.execute(refreshToken);

        boolean secure = tokenProvider.isCookieSecure();
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSitePolicy(secure))
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        boolean secure = tokenProvider.isCookieSecure();
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(tokenProvider.getRefreshTokenMaxAgeSecs())
                .sameSite(sameSitePolicy(secure))
                .build();
    }

    /**
     * SameSite=None exige Secure; los navegadores rechazan una cookie None sin Secure.
     * En dev (http, secure=false) usamos Lax —válido en localhost aunque front y back
     * estén en puertos distintos (mismo site)— para que la cookie sobreviva al reload.
     * En qa/prod (https, secure=true) mantenemos None para soportar cross-site.
     */
    private String sameSitePolicy(boolean secure) {
        return secure ? "None" : "Lax";
    }
}
