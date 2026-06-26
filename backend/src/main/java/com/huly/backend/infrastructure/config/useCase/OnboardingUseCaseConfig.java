package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.onboarding.CompleteOnboardingMapper;
import com.huly.backend.domain.mapper.onboarding.CompleteTutorialMapper;
import com.huly.backend.domain.mapper.onboarding.GenerateOnboardingOptionsMapper;
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
    public CompleteOnboardingMapper completeOnboardingMapper() {
        return new CompleteOnboardingMapper();
    }

    @Bean
    public CompleteTutorialMapper completeTutorialMapper() {
        return new CompleteTutorialMapper();
    }

    @Bean
    public GenerateOnboardingOptionsMapper generateOnboardingOptionsMapper() {
        return new GenerateOnboardingOptionsMapper();
    }

    @Bean
    public CompleteOnboardingUseCase completeOnboardingUseCase(UserRepository userRepository, UserDetailDomainRepository userDetailDomainRepository, UserVectorMemoryService userVectorMemoryService, GrantBadgeUseCase grantBadgeUseCase, CompleteOnboardingMapper completeOnboardingMapper) {
        return new CompleteOnboardingUseCase(userRepository, userDetailDomainRepository, userVectorMemoryService, grantBadgeUseCase, completeOnboardingMapper);
    }

    @Bean
    public CompleteTutorialUseCase completeTutorialUseCase(UserDetailDomainRepository userDetailDomainRepository, CompleteTutorialMapper completeTutorialMapper) {
        return new CompleteTutorialUseCase(userDetailDomainRepository, completeTutorialMapper);
    }

    @Bean
    public GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase(LLMChatPort llmChatPort, GenerateOnboardingOptionsMapper generateOnboardingOptionsMapper) {
        return new GenerateOnboardingOptionsUseCase(llmChatPort, generateOnboardingOptionsMapper);
    }
}
