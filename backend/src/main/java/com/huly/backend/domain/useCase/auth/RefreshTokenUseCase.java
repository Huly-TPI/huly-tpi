package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenPort tokenPort;

    @Transactional
    public AuthTokens execute(String rawToken) {

        if (!tokenPort.isTokenValid(rawToken) || !tokenPort.isRefreshToken(rawToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found or already used"));

        if (isExpired(stored)) {
            refreshTokenRepository.delete(stored);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        Long userId = tokenPort.extractUserId(rawToken);
        if (userId == null) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotActiveException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active");
        }

        refreshTokenRepository.delete(stored);

        String newAccessToken = tokenPort.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String newRefreshToken = tokenPort.generateRefreshToken(user.getId(), user.getEmail());

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
                .expiredAt(now.plusSeconds(tokenPort.getRefreshTokenMaxAgeSecs()))
                .build());
    }
}