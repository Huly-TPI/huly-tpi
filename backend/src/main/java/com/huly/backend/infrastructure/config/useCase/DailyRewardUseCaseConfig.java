package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
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
    public ClaimDailyRewardUseCase claimDailyRewardUseCase(DailyRewardRepository dailyRewardRepository,
                                                           UserDetailDomainRepository userDetailDomainRepository,
                                                           UserPlanRepository userPlanRepository,
                                                           CoinService coinService) {
        return new ClaimDailyRewardUseCase(dailyRewardRepository, userDetailDomainRepository, userPlanRepository, coinService, Clock.system(ZONE));
    }

    @Bean
    public GetDailyRewardStatusUseCase getDailyRewardStatusUseCase(DailyRewardRepository dailyRewardRepository,
                                                                   UserDetailDomainRepository userDetailDomainRepository,
                                                                   UserPlanRepository userPlanRepository) {
        return new GetDailyRewardStatusUseCase(dailyRewardRepository, userDetailDomainRepository, userPlanRepository, Clock.system(ZONE));
    }
}
