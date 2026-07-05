package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityCorrelationStats;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetActivityCorrelationUseCaseTest {

    private ActivityRepository activityRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private GetActivityCorrelationUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        useCase = new GetActivityCorrelationUseCase(activityRepository, emotionalEventRepository);
    }

    @Test
    void execute_shouldCalculateCorrelationStatsCorrectly() {
        Activity breathing = Activity.builder().id(1L).title("Breathing").type(ActivityType.BREATHING).build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing));

        EmotionalEvent event1 = EmotionalEvent.builder()
                .recommendedActivityId(1L)
                .detectedEmotion("ANXIETY")
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .build();
        EmotionalEvent event2 = EmotionalEvent.builder()
                .recommendedActivityId(1L)
                .detectedEmotion("ANXIETY")
                .recommendationDecision(RecommendationDecision.IGNORED)
                .build();
        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(event1, event2));

        List<ActivityCorrelationStats> result = useCase.execute(Timeframe.MONTH);

        // Debería devolver estadísticas para todas las emociones disponibles en EmotionType
        assertThat(result).isNotEmpty();

        ActivityCorrelationStats anxietyStats = result.stream()
                .filter(s -> s.getEmotion().equalsIgnoreCase("Ansiedad"))
                .findFirst().orElseThrow();
        assertThat(anxietyStats.getActivityType()).isEqualTo("BREATHING");
        assertThat(anxietyStats.getSuggestionsCount()).isEqualTo(2);
        assertThat(anxietyStats.getAcceptanceRate()).isEqualTo(50.0);
    }
}
