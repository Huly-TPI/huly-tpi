package com.huly.backend.presentation.controller;

import com.huly.backend.application.auth.dto.LoginRequest;
import com.huly.backend.application.auth.dto.LoginResponse;
import com.huly.backend.application.auth.dto.RegisterRequest;
import com.huly.backend.application.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie.from(
                "refreshToken",
                response.getRefreshToken()
        )
                .httpOnly(true)
                .secure(false) // true en producción con HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(
                    LoginResponse.builder()
                            .accessToken(response.getAccessToken())
                            .build()
            );
    }

    @PostMapping("/register")
        public ResponseEntity<LoginResponse> register(
                @Valid @RequestBody RegisterRequest request
        ) {

        LoginResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
        }
}