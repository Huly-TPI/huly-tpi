package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.provider.EmotionalAnalysisPort;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudRecommendationUseCaseConfig {

    @Bean
    public GetCloudRecommendationUseCase getCloudRecommendationUseCase(
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            GetEmotionalRecommendationsUseCase recommendationsUseCase
    ) {
        return new GetCloudRecommendationUseCase(
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationsUseCase
        );
    }
}
