package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.dailyReward.ClaimDailyRewardMapper;
import com.huly.backend.domain.mapper.dailyReward.GetDailyRewardStatusMapper;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import com.huly.backend.domain.service.user.UserActivityService;
import com.huly.backend.domain.useCase.dailyReward.ClaimDailyRewardUseCase;
import com.huly.backend.domain.useCase.dailyReward.GetDailyRewardStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class DailyRewardUseCaseConfig {

    /** Zona horaria de negocio para definir "el día" del reclamo diario. */
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public ClaimDailyRewardMapper claimDailyRewardMapper() {
        return new ClaimDailyRewardMapper();
    }

    @Bean
    public GetDailyRewardStatusMapper getDailyRewardStatusMapper() {
        return new GetDailyRewardStatusMapper();
    }

    @Bean
    public ClaimDailyRewardUseCase claimDailyRewardUseCase(DailyRewardRepository dailyRewardRepository,
                                                           UserDetailDomainRepository userDetailDomainRepository,
                                                           PlanService planService,
                                                           UserActivityService userActivityService,
                                                           CoinService coinService,
                                                           ClaimDailyRewardMapper claimDailyRewardMapper) {
        return new ClaimDailyRewardUseCase(dailyRewardRepository, userDetailDomainRepository, planService,
                userActivityService, coinService, Clock.system(ZONE), claimDailyRewardMapper);
    }

    @Bean
    public GetDailyRewardStatusUseCase getDailyRewardStatusUseCase(DailyRewardRepository dailyRewardRepository,
                                                                   UserDetailDomainRepository userDetailDomainRepository,
                                                                   PlanService planService,
                                                                   GetDailyRewardStatusMapper getDailyRewardStatusMapper) {
        return new GetDailyRewardStatusUseCase(dailyRewardRepository, userDetailDomainRepository, planService,
                Clock.system(ZONE), getDailyRewardStatusMapper);
    }
}
