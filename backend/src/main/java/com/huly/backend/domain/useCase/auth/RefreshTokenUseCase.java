package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Caso de uso: renovar el accessToken usando el refreshToken.
 * Implementa "token rotation": al renovar, el refreshToken viejo se invalida en BD
 * y se emite uno nuevo, reduciendo el riesgo de reutilización de tokens robados.
 *
 * Valida el token en dos etapas:
 *  1. Verificación criptográfica (firma y expiración via JWT)
 *  2. Verificación en BD (asegura que no fue usado ya o revocado en logout)
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public AuthTokens execute(String rawToken) {
        // Primera validación: firma y expiración del JWT
        if (!tokenProvider.isTokenValid(rawToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Segunda validación: el token debe existir en BD (no fue usado ni revocado)
        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found or already used"));

        String email = tokenProvider.extractEmail(rawToken);
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        // Revoca el refreshToken viejo (token rotation: un token solo sirve una vez)
        refreshTokenRepository.delete(stored);

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        // Persiste el nuevo refreshToken para futuras renovaciones
        Instant now = Instant.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(newRefreshToken)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenProvider.getRefreshTokenMaxAgeSecs()))
                .build());

        return AuthTokens.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
