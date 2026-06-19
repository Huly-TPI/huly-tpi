package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.LLMChatPort;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.badge.GrantBadgeUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OnboardingUseCaseConfig {

    @Bean
    public CompleteOnboardingUseCase completeOnboardingUseCase(UserRepository userRepository, UserDetailDomainRepository userDetailDomainRepository, UserVectorMemoryService userVectorMemoryService, GrantBadgeUseCase grantBadgeUseCase) {
        return new CompleteOnboardingUseCase(userRepository, userDetailDomainRepository, userVectorMemoryService, grantBadgeUseCase);
    }

    @Bean
    public CompleteTutorialUseCase completeTutorialUseCase(UserDetailDomainRepository userDetailDomainRepository) {
        return new CompleteTutorialUseCase(userDetailDomainRepository);
    }

    @Bean
    public GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase(LLMChatPort llmChatPort) {
        return new GenerateOnboardingOptionsUseCase(llmChatPort);
    }
}
