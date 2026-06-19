package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudRecommendationUseCaseConfig {

    @Bean
    public GetCloudRecommendationUseCase getCloudRecommendationUseCase(
            @org.springframework.beans.factory.annotation.Value("classpath:/prompts/cloud-analysis.st") org.springframework.core.io.Resource cloudAnalysisPrompt,
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            EmotionalRecommendationService recommendationService,
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository
    ) {
        return new GetCloudRecommendationUseCase(
                cloudAnalysisPrompt,
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationService,
                activityRepository,
                emotionalEventRepository
        );
    }
}
