package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.useCase.lanternRecommendation.GetLanternRecommendationUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LanternRecommendationUseCaseConfig {

    @Bean
    public GetLanternRecommendationUseCase getLanternRecommendationUseCase(
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            EmotionalRecommendationService recommendationService,
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new GetLanternRecommendationUseCase(
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationService,
                activityRepository,
                emotionalEventRepository
        );
    }
}
