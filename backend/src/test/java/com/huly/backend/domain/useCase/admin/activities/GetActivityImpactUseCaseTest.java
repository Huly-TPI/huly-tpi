package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityImpactStats;
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

class GetActivityImpactUseCaseTest {

    private ActivityRepository activityRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private GetActivityImpactUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        useCase = new GetActivityImpactUseCase(activityRepository, emotionalEventRepository);
    }

    @Test
    void execute_shouldReturnImpactStatsBasedOnMetrics() {
        Activity breathing = Activity.builder()
                .id(1L)
                .title("Breathing")
                .type(ActivityType.BREATHING)
                .effectValence(0.5)
                .effectArousal(-0.2)
                .build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing));

        Instant baseTime = Instant.now();
        EmotionalEvent recommendation = EmotionalEvent.builder()
                .id(10L)
                .userId(100L)
                .recommendedActivityId(1L)
                .valence(0.0)
                .arousal(0.8)
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .createdAt(baseTime)
                .build();

        EmotionalEvent nextState = EmotionalEvent.builder()
                .id(11L)
                .userId(100L)
                .valence(0.6)
                .arousal(0.3)
                .createdAt(baseTime.plusSeconds(300))
                .build();

        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(recommendation));
        when(emotionalEventRepository.findByUserIds(List.of(100L))).thenReturn(List.of(recommendation, nextState));

        List<ActivityImpactStats> result = useCase.execute(Timeframe.MONTH);

        assertThat(result).hasSize(1);
        ActivityImpactStats stats = result.get(0);
        assertThat(stats.getActivityType()).isEqualTo("BREATHING");
        assertThat(stats.isBasedOnMetrics()).isTrue();
        // Delta valencia = 0.6 - 0.0 = 0.6
        assertThat(stats.getAverageValenceChange()).isEqualTo(0.6);
        // Delta arousal = 0.3 - 0.8 = -0.5
        assertThat(stats.getAverageArousalChange()).isEqualTo(-0.5);
    }

    @Test
    void execute_shouldFallbackToDefaultConfig_whenNoMetricsAvailable() {
        Activity breathing = Activity.builder()
                .id(1L)
                .title("Breathing")
                .type(ActivityType.BREATHING)
                .effectValence(0.3)
                .effectArousal(-0.1)
                .build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing));
        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of());

        List<ActivityImpactStats> result = useCase.execute(Timeframe.MONTH);

        assertThat(result).hasSize(1);
        ActivityImpactStats stats = result.get(0);
        assertThat(stats.isBasedOnMetrics()).isFalse();
        assertThat(stats.getAverageValenceChange()).isEqualTo(0.3);
        assertThat(stats.getAverageArousalChange()).isEqualTo(-0.1);
    }
}
