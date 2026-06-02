package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.repository.UserDetailDomainRepository;

import java.time.Instant;

/**
 * Caso de uso: autenticar un usuario con email y contraseña.
 * Si las credenciales son válidas y la cuenta está activa, genera un par de tokens
 * (accessToken + refreshToken) y persiste el refreshToken en BD para poder invalidarlo en logout.
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final PasswordHasher passwordHasher;
    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public AuthTokens execute(String email, String rawPassword) {
        // El mensaje de error es genérico a propósito: no revela si el email existe o no (evita user enumeration)
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordHasher.matches(rawPassword, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        // Persiste el refreshToken para poder validarlo y revocarlo en logout/refresh
        Instant now = Instant.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenProvider.getRefreshTokenMaxAgeSecs()))
                .build());

        // Informa al frontend si el usuario ya completó el onboarding de perfil
        Boolean profileOnBoardingCompleted = userDetailDomainRepository.findProfileOnBoardingCompleted(user.getId()).orElse(false);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .profileOnBoardingCompleted(profileOnBoardingCompleted)
                .build();
    }
}
