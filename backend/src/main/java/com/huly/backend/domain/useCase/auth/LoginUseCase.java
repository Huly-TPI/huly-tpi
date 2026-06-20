package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.AccountNotActiveException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.port.TokenPort;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.payment.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenPort tokenPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final CoinService coinService;
    private final Clock clock;
    private final int inactivityThresholdDays;
    private final int comebackRewardCoins;

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

        String accessToken = tokenPort.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus()
        );
        String refreshToken = tokenPort.generateRefreshToken(user.getId(), user.getEmail());

        Instant now = clock.instant();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .createdAt(now)
                .expiredAt(now.plusSeconds(tokenPort.getRefreshTokenMaxAgeSecs()))
                .build());

        int comebackReward = resolveComebackReward(user, now);
        userDetailDomainRepository.updateLastLoginDate(user.getId(), now);

        Boolean onBoardingCompleted = userDetailDomainRepository.findOnBoardingCompleted(user.getId()).orElse(false);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .onBoardingCompleted(onBoardingCompleted)
                .comebackReward(comebackReward > 0 ? comebackReward : null)
                .build();
    }

    /**
     * Recompensa de retorno: si un usuario regular vuelve tras {@code inactivityThresholdDays}+
     * días sin loguearse, se le acreditan monedas. Solo aplica al rol USER.
     */
    private int resolveComebackReward(AppUser user, Instant now) {
        if (user.getRole() != UserRole.USER) {
            return 0;
        }
        Optional<Instant> lastLogin = userDetailDomainRepository.findLastLoginDate(user.getId());
        if (lastLogin.isEmpty()) {
            return 0;
        }
        ZoneId zone = clock.getZone();
        long daysInactive = ChronoUnit.DAYS.between(
                lastLogin.get().atZone(zone).toLocalDate(),
                now.atZone(zone).toLocalDate());
        if (daysInactive < inactivityThresholdDays) {
            return 0;
        }
        coinService.credit(user.getId(), comebackRewardCoins);
        return comebackRewardCoins;
    }
}
