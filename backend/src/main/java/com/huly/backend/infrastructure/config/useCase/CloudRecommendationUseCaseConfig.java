package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.cloud.CreateCloudThoughtMapper;
import com.huly.backend.domain.mapper.cloud.ListCloudThoughtsMapper;
import com.huly.backend.domain.mapper.cloud.MarkCloudWorkedOnMapper;
import com.huly.backend.domain.mapper.cloud.UpdateCloudStatusMapper;
import com.huly.backend.domain.mapper.cloudRecommendation.GetCloudRecommendationMapper;
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
    public CreateCloudThoughtMapper createCloudThoughtMapper() {
        return new CreateCloudThoughtMapper();
    }

    @Bean
    public ListCloudThoughtsMapper listCloudThoughtsMapper() {
        return new ListCloudThoughtsMapper();
    }

    @Bean
    public UpdateCloudStatusMapper updateCloudStatusMapper() {
        return new UpdateCloudStatusMapper();
    }

    @Bean
    public MarkCloudWorkedOnMapper markCloudWorkedOnMapper() {
        return new MarkCloudWorkedOnMapper();
    }

    @Bean
    public GetCloudRecommendationMapper getCloudRecommendationMapper() {
        return new GetCloudRecommendationMapper();
    }

    @Bean
    public GetCloudRecommendationUseCase getCloudRecommendationUseCase(
            @Value("classpath:/prompts/cloud-analysis.st") Resource cloudAnalysisPrompt,
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            EmotionalRecommendationService recommendationService,
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository,
            GetCloudRecommendationMapper getCloudRecommendationMapper
    ) {
        return new GetCloudRecommendationUseCase(
                cloudAnalysisPrompt,
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationService,
                activityRepository,
                emotionalEventRepository,
                getCloudRecommendationMapper
        );
    }

    @Bean
    public CreateCloudThoughtUseCase createCloudThoughtUseCase(CloudThoughtRepository cloudThoughtRepository,
                                                               CreateCloudThoughtMapper createCloudThoughtMapper) {
        return new CreateCloudThoughtUseCase(cloudThoughtRepository, createCloudThoughtMapper);
    }

    @Bean
    public ListCloudThoughtsUseCase listCloudThoughtsUseCase(CloudThoughtRepository cloudThoughtRepository,
                                                             ListCloudThoughtsMapper listCloudThoughtsMapper) {
        return new ListCloudThoughtsUseCase(cloudThoughtRepository, listCloudThoughtsMapper);
    }

    @Bean
    public UpdateCloudStatusUseCase updateCloudStatusUseCase(CloudThoughtRepository cloudThoughtRepository,
                                                             UpdateCloudStatusMapper updateCloudStatusMapper) {
        return new UpdateCloudStatusUseCase(cloudThoughtRepository, updateCloudStatusMapper);
    }

    @Bean
    public MarkCloudWorkedOnUseCase markCloudWorkedOnUseCase(CloudThoughtRepository cloudThoughtRepository,
                                                             MarkCloudWorkedOnMapper markCloudWorkedOnMapper) {
        return new MarkCloudWorkedOnUseCase(cloudThoughtRepository, markCloudWorkedOnMapper);
    }
}
