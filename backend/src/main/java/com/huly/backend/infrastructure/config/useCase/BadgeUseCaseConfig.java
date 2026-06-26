package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.badge.GetAllBadgesMapper;
import com.huly.backend.domain.mapper.badge.GetUserBadgesMapper;
import com.huly.backend.domain.mapper.badge.GrantBadgeMapper;
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
    public GetAllBadgesMapper getAllBadgesMapper() {
        return new GetAllBadgesMapper();
    }

    @Bean
    public GetUserBadgesMapper getUserBadgesMapper() {
        return new GetUserBadgesMapper();
    }

    @Bean
    public GrantBadgeMapper grantBadgeMapper() {
        return new GrantBadgeMapper();
    }

    @Bean
    public GetAllBadgesUseCase getAllBadgesUseCase(BadgeRepository badgeRepository,
                                                   GetAllBadgesMapper getAllBadgesMapper) {
        return new GetAllBadgesUseCase(badgeRepository, getAllBadgesMapper);
    }

    @Bean
    public GetUserBadgesUseCase getUserBadgesUseCase(UserBadgeRepository userBadgeRepository,
                                                     GetUserBadgesMapper getUserBadgesMapper) {
        return new GetUserBadgesUseCase(userBadgeRepository, getUserBadgesMapper);
    }

    @Bean
    public GrantBadgeUseCase grantBadgeUseCase(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository, UserRepository userRepository,
                                               GrantBadgeMapper grantBadgeMapper) {
        return new GrantBadgeUseCase(badgeRepository, userBadgeRepository, userRepository, grantBadgeMapper);
    }
}
