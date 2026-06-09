package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.provider.TokenProvider;
import com.huly.backend.domain.repository.RefreshTokenRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.repository.UserDetailDomainRepository;

import java.time.Instant;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final PasswordHasher passwordHasher;
    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public AuthTokens execute(String email, String rawPassword) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordHasher.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active");
        }

        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        Instant now = Instant.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenProvider.getRefreshTokenMaxAgeSecs()))
                .build());

        Boolean onBoardingCompleted = userDetailDomainRepository.findOnBoardingCompleted(user.getId()).orElse(false);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .onBoardingCompleted(onBoardingCompleted)
                .build();
    }
}
