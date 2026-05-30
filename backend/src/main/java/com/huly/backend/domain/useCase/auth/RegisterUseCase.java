package com.huly.backend.domain.useCase.auth;

import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.presentation.dto.auth.RegisterRequest;
import com.huly.backend.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUseCase {

    private final AuthService authService;

    public LoginResponse execute(RegisterRequest request) {
        return authService.register(request);
    }
}