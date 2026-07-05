package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityPopularityStats;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetActivityPopularityUseCaseTest {

    private ActivityRepository activityRepository;
    private ActivitySessionRepository activitySessionRepository;
    private GetActivityPopularityUseCase useCase;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        activitySessionRepository = mock(ActivitySessionRepository.class);
        useCase = new GetActivityPopularityUseCase(activityRepository, activitySessionRepository);
    }

    @Test
    void execute_shouldReturnCorrectPopularityStats() {
        Activity breathing = Activity.builder().id(1L).title("Breathing Guided").type(ActivityType.BREATHING).build();
        Activity diary = Activity.builder().id(2L).title("Diary Guided").type(ActivityType.DIARY).build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing, diary));

        ActivitySession session1 = ActivitySession.builder().activityType(ActivityType.BREATHING).createdAt(Instant.now()).build();
        ActivitySession session2 = ActivitySession.builder().activityType(ActivityType.BREATHING).createdAt(Instant.now()).build();
        ActivitySession session3 = ActivitySession.builder().activityType(ActivityType.DIARY).createdAt(Instant.now()).build();
        when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of(session1, session2, session3));

        List<ActivityPopularityStats> result = useCase.execute(Timeframe.MONTH);

        assertThat(result).hasSize(2);
        
        ActivityPopularityStats breathingStats = result.stream()
                .filter(s -> s.getActivityType().equals("BREATHING"))
                .findFirst().orElseThrow();
        assertThat(breathingStats.getActivityName()).isEqualTo("Breathing Guided");
        assertThat(breathingStats.getTotalSessions()).isEqualTo(2L);

        ActivityPopularityStats diaryStats = result.stream()
                .filter(s -> s.getActivityType().equals("DIARY"))
                .findFirst().orElseThrow();
        assertThat(diaryStats.getActivityName()).isEqualTo("Diary Guided");
        assertThat(diaryStats.getTotalSessions()).isEqualTo(1L);
    }

    @Test
    void execute_shouldHandleEmptySessionsCorrectly() {
        Activity breathing = Activity.builder().id(1L).title("Breathing Guided").type(ActivityType.BREATHING).build();
        when(activityRepository.findAll()).thenReturn(List.of(breathing));
        when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of());

        List<ActivityPopularityStats> result = useCase.execute(Timeframe.TODAY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalSessions()).isEqualTo(0L);
    }
}
