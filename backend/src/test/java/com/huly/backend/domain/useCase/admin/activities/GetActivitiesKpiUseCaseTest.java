package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.ActivitiesKpiStats;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetActivitiesKpiUseCaseTest {

    private ActivityRepository activityRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private ActivitySessionRepository activitySessionRepository;
    private GetActivitiesKpiUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        activitySessionRepository = mock(ActivitySessionRepository.class);
        useCase = new GetActivitiesKpiUseCase(activityRepository, emotionalEventRepository, activitySessionRepository);
    }

    @Test
    void execute_shouldCalculateKpisCorrectly() {
        Activity breathing = Activity.builder().id(1L).title("Breathing").type(ActivityType.BREATHING).build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing));

        ActivitySession session1 = ActivitySession.builder().activityType(ActivityType.BREATHING).build();
        ActivitySession session2 = ActivitySession.builder().activityType(ActivityType.BREATHING).build();
        when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of(session1, session2));

        Instant baseTime = Instant.now();
        EmotionalEvent recommendation = EmotionalEvent.builder()
                .id(10L)
                .userId(100L)
                .recommendedActivityId(1L)
                .valence(0.0)
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .createdAt(baseTime)
                .build();

        EmotionalEvent nextState = EmotionalEvent.builder()
                .id(11L)
                .userId(100L)
                .valence(0.5)
                .createdAt(baseTime.plusSeconds(300))
                .build();

        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(recommendation));
        when(emotionalEventRepository.findByUserIds(List.of(100L))).thenReturn(List.of(recommendation, nextState));

        ActivitiesKpiStats result = useCase.execute(Timeframe.MONTH);

        assertThat(result.getTotalSessions()).isEqualTo(2);
        assertThat(result.getTopActivityType()).isEqualTo("BREATHING");
        assertThat(result.getTopActivitySessions()).isEqualTo(2);
        assertThat(result.getAverageMoodImprovement()).isEqualTo(100.0);
    }
}
