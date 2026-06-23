package com.huly.backend.domain.repository.auth;

import com.huly.backend.domain.model.auth.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void delete(RefreshToken refreshToken);
}
