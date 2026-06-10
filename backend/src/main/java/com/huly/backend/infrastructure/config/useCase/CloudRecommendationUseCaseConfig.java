package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudRecommendationUseCaseConfig {

    @Bean
    public GetCloudRecommendationUseCase getCloudRecommendationUseCase(LLMChatPort llmChatPort) {
        return new GetCloudRecommendationUseCase(llmChatPort);
    }
}
