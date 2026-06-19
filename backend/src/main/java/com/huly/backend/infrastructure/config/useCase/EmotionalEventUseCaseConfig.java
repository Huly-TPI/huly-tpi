package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.repository.UserEmotionalStateRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
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
    public CreateEmotionalEventUseCase createEmotionalEventUseCase(EmotionalEventRepository emotionalEventRepository, ActivityRepository activityRepository) {
        return new CreateEmotionalEventUseCase(emotionalEventRepository, activityRepository);
    }

    @Bean
    public GetEmotionalRecommendationsUseCase getEmotionalRecommendationsUseCase(
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository,
            EmotionalRecommendationService emotionalRecommendationService
    ) {
        return new GetEmotionalRecommendationsUseCase(
                activityRepository,
                emotionalEventRepository,
                emotionalRecommendationService
        );
    }

    @Bean
    public SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase(UserEmotionalStateRepository userEmotionalStateRepository) {
        return new SaveUserEmotionalStateUseCase(userEmotionalStateRepository);
    }

    @Bean
    public UpdateEmotionalEventDecisionUseCase updateEmotionalEventDecisionUseCase(EmotionalEventRepository emotionalEventRepository, ActivityRepository activityRepository, UserVectorMemoryService userVectorMemoryService) {
        return new UpdateEmotionalEventDecisionUseCase(emotionalEventRepository, activityRepository, userVectorMemoryService);
    }

    @Bean
    public UpdateEmotionalEventFeedbackUseCase updateEmotionalEventFeedbackUseCase(EmotionalEventRepository emotionalEventRepository) {
        return new UpdateEmotionalEventFeedbackUseCase(emotionalEventRepository);
    }
}
