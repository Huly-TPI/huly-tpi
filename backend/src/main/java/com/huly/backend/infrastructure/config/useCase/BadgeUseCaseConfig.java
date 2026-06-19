package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.badge.BadgeRepository;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BadgeUseCaseConfig {

    @Bean
    public GetAllBadgesUseCase getAllBadgesUseCase(BadgeRepository badgeRepository) {
        return new GetAllBadgesUseCase(badgeRepository);
    }

    @Bean
    public GetUserBadgesUseCase getUserBadgesUseCase(UserBadgeRepository userBadgeRepository) {
        return new GetUserBadgesUseCase(userBadgeRepository);
    }

    @Bean
    public GrantBadgeUseCase grantBadgeUseCase(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository, UserRepository userRepository) {
        return new GrantBadgeUseCase(badgeRepository, userBadgeRepository, userRepository);
    }
}
