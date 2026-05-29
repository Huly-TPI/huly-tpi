package com.huly.backend.application.auth.service;

import com.huly.backend.application.auth.dto.LoginRequest;
import com.huly.backend.application.auth.dto.LoginResponse;
import com.huly.backend.application.auth.dto.RegisterRequest;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        System.out.println("EMAIL: " + request.getEmail());

        AppUserEntity user = appUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        System.out.println("USER: " + user);

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken =
                jwtService.generateAccessToken(user.getEmail());

        String refreshToken =
                jwtService.generateRefreshToken(user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public void register(RegisterRequest request) {

        boolean userExists = appUserRepository
                .existsByEmail(request.getEmail());

        if (userExists) {
                throw new ConflictException("Email already in use");
        }

        AppUserEntity user = new AppUserEntity();

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        appUserRepository.save(user);
    }
}