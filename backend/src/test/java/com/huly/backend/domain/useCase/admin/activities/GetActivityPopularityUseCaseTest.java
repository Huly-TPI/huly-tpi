package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityPopularityStats;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.huly.backend.domain.model.enums.ActivityType.BREATHING;
import static com.huly.backend.domain.model.enums.ActivityType.DIARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActivityPopularityUseCaseTest {

    private static final Instant BASE = Instant.parse("2026-06-12T10:00:00Z");

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivitySessionRepository activitySessionRepository;

    @InjectMocks
    private GetActivityPopularityUseCase useCase;

    @Test
    @DisplayName("Devuelve el conteo de sesiones por actividad")
    void executeShouldReturnCorrectPopularityStats() {
        // --- arrange ---
        givenTwoActivitiesWithMixedSessions();
        // --- act ---
        List<ActivityPopularityStats> result = calculatePopularity(Timeframe.MONTH);
        // --- assert ---
        thenPopularityHasSize(result, 2);
        thenActivityPopularityIs(result, "BREATHING", "Breathing Guided", 2L);
        thenActivityPopularityIs(result, "DIARY", "Diary Guided", 1L);
    }

    @Test
    @DisplayName("Devuelve cero sesiones para las actividades sin sesiones registradas")
    void executeShouldHandleEmptySessionsCorrectly() {
        // --- arrange ---
        givenSingleActivityWithNoSessions();
        // --- act ---
        List<ActivityPopularityStats> result = calculatePopularity(Timeframe.TODAY);
        // --- assert ---
        thenPopularityHasSize(result, 1);
        thenActivityPopularityIs(result, "BREATHING", "Breathing Guided", 0L);
    }

    // --- arrange ---

    private void givenTwoActivitiesWithMixedSessions() {
        givenActivities(activity(1L, BREATHING, "Breathing Guided"), activity(2L, DIARY, "Diary Guided"));
        givenSessions(session(BREATHING), session(BREATHING), session(DIARY));
    }

    private void givenSingleActivityWithNoSessions() {
        givenActivities(activity(1L, BREATHING, "Breathing Guided"));
        givenSessions();
    }

    private void givenActivities(Activity... activities) {
        when(activityRepository.findAll()).thenReturn(List.of(activities));
    }

    private void givenSessions(ActivitySession... sessions) {
        when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of(sessions));
    }

    private Activity activity(long id, ActivityType type, String title) {
        return Activity.builder().id(id).type(type).title(title).build();
    }

    private ActivitySession session(ActivityType type) {
        return ActivitySession.builder().activityType(type).createdAt(BASE).build();
    }

    // --- act ---

    private List<ActivityPopularityStats> calculatePopularity(Timeframe timeframe) {
        return useCase.execute(timeframe);
    }

    // --- assert ---

    private void thenPopularityHasSize(List<ActivityPopularityStats> result, int size) {
        assertThat(result).hasSize(size);
    }

    private void thenActivityPopularityIs(List<ActivityPopularityStats> result, String activityType,
                                          String activityName, long totalSessions) {
        ActivityPopularityStats stats = findByType(result, activityType);
        assertThat(stats.getActivityName()).isEqualTo(activityName);
        assertThat(stats.getTotalSessions()).isEqualTo(totalSessions);
    }

    private ActivityPopularityStats findByType(List<ActivityPopularityStats> result, String activityType) {
        return result.stream()
                .filter(s -> s.getActivityType().equals(activityType))
                .findFirst()
                .orElseThrow();
    }
}
