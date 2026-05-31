package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;

public interface TokenProvider {
    String generateAccessToken(Long userId, String email, UserRole role, UserStatus status);
    String generateRefreshToken(Long userId, String email);
    boolean isTokenValid(String token);
    String extractEmail(String token);
    long getRefreshTokenMaxAgeSecs();
    boolean isCookieSecure();
}
