package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.user.SendInactivityRemindersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class InactivityUseCaseConfig {

    @Bean
    public SendInactivityRemindersUseCase sendInactivityRemindersUseCase(
            UserDetailDomainRepository userDetailDomainRepository, EmailPort emailPort) {
        return new SendInactivityRemindersUseCase(
                userDetailDomainRepository, emailPort, Clock.system(RewardPolicy.ZONE),
                RewardPolicy.INACTIVITY_THRESHOLD_DAYS, RewardPolicy.COMEBACK_REWARD_COINS);
    }
}
