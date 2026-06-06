package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public AuthTokens execute(String rawToken) {

        if (!tokenProvider.isTokenValid(rawToken) || !tokenProvider.isRefreshToken(rawToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found or already used"));

        if (isExpired(stored)) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Refresh token expired");
        }

        Long userId = tokenProvider.extractUserId(rawToken);
        if (userId == null) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        refreshTokenRepository.delete(stored);

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        persistRefreshToken(user.getId(), newRefreshToken);

        return AuthTokens.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private boolean isExpired(RefreshToken token) {
        return token.getExpiredAt() == null || token.getExpiredAt().isBefore(Instant.now());
    }

    private void persistRefreshToken(Long userId, String token) {
        Instant now = Instant.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .token(token)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenProvider.getRefreshTokenMaxAgeSecs()))
                .build());
    }
}