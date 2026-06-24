package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.CloudThoughtRepository;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.domain.useCase.cloud.CreateCloudThoughtUseCase;
import com.huly.backend.domain.useCase.cloud.ListCloudThoughtsUseCase;
import com.huly.backend.domain.useCase.cloud.MarkCloudWorkedOnUseCase;
import com.huly.backend.domain.useCase.cloud.UpdateCloudStatusUseCase;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class CloudRecommendationUseCaseConfig {

    @Bean
    public GetCloudRecommendationUseCase getCloudRecommendationUseCase(
            @Value("classpath:/prompts/cloud-analysis.st") Resource cloudAnalysisPrompt,
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

    @Bean
    public CreateCloudThoughtUseCase createCloudThoughtUseCase(CloudThoughtRepository cloudThoughtRepository) {
        return new CreateCloudThoughtUseCase(cloudThoughtRepository);
    }

    @Bean
    public ListCloudThoughtsUseCase listCloudThoughtsUseCase(CloudThoughtRepository cloudThoughtRepository) {
        return new ListCloudThoughtsUseCase(cloudThoughtRepository);
    }

    @Bean
    public UpdateCloudStatusUseCase updateCloudStatusUseCase(CloudThoughtRepository cloudThoughtRepository) {
        return new UpdateCloudStatusUseCase(cloudThoughtRepository);
    }

    @Bean
    public MarkCloudWorkedOnUseCase markCloudWorkedOnUseCase(CloudThoughtRepository cloudThoughtRepository) {
        return new MarkCloudWorkedOnUseCase(cloudThoughtRepository);
    }
}
