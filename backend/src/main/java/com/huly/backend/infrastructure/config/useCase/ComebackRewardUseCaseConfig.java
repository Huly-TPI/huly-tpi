package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.useCase.comebackReward.ClaimComebackRewardUseCase;
import com.huly.backend.domain.useCase.comebackReward.GetComebackRewardStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ComebackRewardUseCaseConfig {

    /** Zona horaria de negocio para definir "el día" de la inactividad. */
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public GetComebackRewardStatusUseCase getComebackRewardStatusUseCase(UserDetailDomainRepository userDetailDomainRepository) {
        return new GetComebackRewardStatusUseCase(userDetailDomainRepository, Clock.system(ZONE));
    }

    @Bean
    public ClaimComebackRewardUseCase claimComebackRewardUseCase(UserDetailDomainRepository userDetailDomainRepository,
                                                                 CoinService coinService) {
        return new ClaimComebackRewardUseCase(userDetailDomainRepository, coinService, Clock.system(ZONE));
    }
}
