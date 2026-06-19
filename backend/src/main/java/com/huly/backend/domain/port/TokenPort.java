package com.huly.backend.domain.port;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;

public interface TokenPort {
    String generateAccessToken(Long userId, String email, UserRole role, UserStatus status);
    String generateRefreshToken(Long userId, String email);
    boolean isTokenValid(String token);
    boolean isAccessToken(String token);
    boolean isRefreshToken(String token);
    String extractEmail(String token);
    Long extractUserId(String token);
    long getRefreshTokenMaxAgeSecs();
    boolean isCookieSecure();
}