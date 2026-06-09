package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(String rawToken) {
        if (rawToken != null) {
            refreshTokenRepository.findByToken(rawToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }
}
