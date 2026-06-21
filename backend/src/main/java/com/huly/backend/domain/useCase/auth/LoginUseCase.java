package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenPort tokenPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final Clock clock;

    @Transactional
    public AuthTokens execute(String email, String rawPassword) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordHasherPort.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active");
        }

        registerActivity(user.getId());

        String accessToken = tokenPort.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String refreshToken = tokenPort.generateRefreshToken(user.getId(), user.getEmail());

        Instant now = Instant.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenPort.getRefreshTokenMaxAgeSecs()))
                .build());

        userRepository.updateLastLogin(user.getId());

        Boolean onBoardingCompleted = userDetailDomainRepository.findOnBoardingCompleted(user.getId()).orElse(false);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .onBoardingCompleted(onBoardingCompleted)
                .build();
    }

    /**
     * Registra la actividad de hoy avanzando {@code last_login_date}, salvo que el usuario tenga un
     * comeback pendiente (brecha de inactividad ya alcanzada): en ese caso no lo toca, para que la
     * recompensa de regreso sobreviva al re-login y se pueda reclamar.
     */
    private void registerActivity(Long userId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate lastSeen = userDetailDomainRepository.findLastLoginDate(userId).orElse(null);
        if (ComebackRewardPolicy.shouldRegisterActivity(lastSeen, today)) {
            userDetailDomainRepository.updateLastLoginDate(userId, today);
        }
    }
}
