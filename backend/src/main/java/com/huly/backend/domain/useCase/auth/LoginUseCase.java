package com.huly.backend.domain.useCase.auth;

import com.huly.backend.presentation.dto.auth.LoginRequest;
import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthService authService;

    public LoginResponse execute(LoginRequest request) {
        return authService.login(request);
    }
}