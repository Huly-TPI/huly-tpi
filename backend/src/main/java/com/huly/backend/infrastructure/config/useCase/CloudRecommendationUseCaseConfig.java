package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.EmotionalAnalysisPort;
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
            @org.springframework.beans.factory.annotation.Value("classpath:/prompts/cloud-analysis.st") org.springframework.core.io.Resource cloudAnalysisPrompt,
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            GetEmotionalRecommendationsUseCase recommendationsUseCase
    ) {
        return new GetCloudRecommendationUseCase(
                cloudAnalysisPrompt,
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationsUseCase
        );
    }
}
