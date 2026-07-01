package com.huly.backend.domain.repository.auth;

import com.huly.backend.domain.model.auth.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String token);
    void delete(PasswordResetToken token);
    void deleteAllByUserId(Long userId);
}
