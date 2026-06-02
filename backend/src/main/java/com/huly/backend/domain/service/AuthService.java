package com.huly.backend.domain.service;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.security.JwtService;
import com.huly.backend.presentation.dto.auth.LoginRequest;
import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.presentation.dto.auth.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.springframework.transaction.annotation.Transactional;


/**
 * Servicio de autenticación LEGACY. Esta clase fue reemplazada por los use cases
 * LoginUseCase y RegisterUseCase que siguen arquitectura limpia.
 *
 * PROBLEMAS de esta implementación (se mantiene por compatibilidad):
 *  - Opera sobre entidades JPA directamente en lugar de modelos de dominio
 *  - Lanza RuntimeException genérica en lugar de UnauthorizedException (401)
 *  - El login devuelve LoginResponse en lugar de AuthTokens (acoplado a la capa de presentación)
 *  - No persiste el refreshToken en BD (no soporta revocación ni token rotation)
 *  - No verifica el estado del usuario (ACTIVE) antes de hacer login
 *
 * Ver LoginUseCase y RegisterUseCase para la implementación correcta.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailRepository userDetailRepository;

    /** @deprecated Usar LoginUseCase en su lugar. */
    public LoginResponse login(LoginRequest request) {

        AppUserEntity user = appUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials") // Debería ser UnauthorizedException
                );

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid credentials"); // Debería ser UnauthorizedException
        }

        String accessToken =
                jwtService.generateAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus()
                );

        String refreshToken =
                jwtService.generateRefreshToken(
                        user.getId(),
                        user.getEmail()
                );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // No persiste en BD: no soporta revocación
                .build();
    }

    /** @deprecated Usar RegisterUseCase en su lugar. */
    @Transactional
    public LoginResponse register(RegisterRequest request) {

        boolean userExists = appUserRepository
                .existsByEmail(request.getEmail());

        if (userExists) {
            throw new ConflictException("El email ya esta registrado");
        }

        AppUserEntity user = new AppUserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        AppUserEntity savedUser = appUserRepository.save(user);

        UserDetailEntity userDetail = UserDetailEntity.builder()
                .appUser(savedUser)
                .name(request.getName())
                .birth(request.getBirthDate())
                .build();

        userDetailRepository.save(userDetail);

        String accessToken =
                jwtService.generateAccessToken(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getRole(),
                        savedUser.getStatus()
                );

        String refreshToken =
                jwtService.generateRefreshToken(
                        savedUser.getId(),
                        savedUser.getEmail()
                );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // No persiste en BD: no soporta revocación
                .build();
    }
}