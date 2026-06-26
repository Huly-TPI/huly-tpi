package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.emotionalEvent.CreateEmotionalEventMapper;
import com.huly.backend.domain.mapper.emotionalEvent.SaveUserEmotionalStateMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventDecisionMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventFeedbackMapper;
import com.huly.backend.domain.mapper.emotionalRecommendation.GetEmotionalRecommendationsMapper;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.repository.user.UserEmotionalStateRepository;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.SaveUserEmotionalStateUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmotionalEventUseCaseConfig {

    @Bean
    public CreateEmotionalEventMapper createEmotionalEventMapper() {
        return new CreateEmotionalEventMapper();
    }

    @Bean
    public UpdateEmotionalEventDecisionMapper updateEmotionalEventDecisionMapper() {
        return new UpdateEmotionalEventDecisionMapper();
    }

    @Bean
    public UpdateEmotionalEventFeedbackMapper updateEmotionalEventFeedbackMapper() {
        return new UpdateEmotionalEventFeedbackMapper();
    }

    @Bean
    public SaveUserEmotionalStateMapper saveUserEmotionalStateMapper() {
        return new SaveUserEmotionalStateMapper();
    }

    @Bean
    public GetEmotionalRecommendationsMapper getEmotionalRecommendationsMapper() {
        return new GetEmotionalRecommendationsMapper();
    }

    @Bean
    public CreateEmotionalEventUseCase createEmotionalEventUseCase(EmotionalEventRepository emotionalEventRepository,
                                                                  ActivityRepository activityRepository,
                                                                  CreateEmotionalEventMapper createEmotionalEventMapper) {
        return new CreateEmotionalEventUseCase(emotionalEventRepository, activityRepository, createEmotionalEventMapper);
    }

    @Bean
    public GetEmotionalRecommendationsUseCase getEmotionalRecommendationsUseCase(
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository,
            EmotionalRecommendationService emotionalRecommendationService,
            GetEmotionalRecommendationsMapper getEmotionalRecommendationsMapper
    ) {
        return new GetEmotionalRecommendationsUseCase(
                activityRepository,
                emotionalEventRepository,
                emotionalRecommendationService,
                getEmotionalRecommendationsMapper
        );
    }

    @Bean
    public SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase(UserEmotionalStateRepository userEmotionalStateRepository,
                                                                       SaveUserEmotionalStateMapper saveUserEmotionalStateMapper) {
        return new SaveUserEmotionalStateUseCase(userEmotionalStateRepository, saveUserEmotionalStateMapper);
    }

    @Bean
    public UpdateEmotionalEventDecisionUseCase updateEmotionalEventDecisionUseCase(EmotionalEventRepository emotionalEventRepository,
                                                                                   ActivityRepository activityRepository,
                                                                                   UserVectorMemoryService userVectorMemoryService,
                                                                                   ChatMessageRepository chatMessageRepository,
                                                                                   UpdateEmotionalEventDecisionMapper updateEmotionalEventDecisionMapper) {
        return new UpdateEmotionalEventDecisionUseCase(emotionalEventRepository, activityRepository, userVectorMemoryService, chatMessageRepository, updateEmotionalEventDecisionMapper);
    }

    @Bean
    public UpdateEmotionalEventFeedbackUseCase updateEmotionalEventFeedbackUseCase(EmotionalEventRepository emotionalEventRepository,
                                                                                   UpdateEmotionalEventFeedbackMapper updateEmotionalEventFeedbackMapper) {
        return new UpdateEmotionalEventFeedbackUseCase(emotionalEventRepository, updateEmotionalEventFeedbackMapper);
    }
}
