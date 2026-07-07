package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityImpactStats;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.huly.backend.domain.model.enums.ActivityType.BREATHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActivityImpactUseCaseTest {

    private static final Instant BASE = Instant.parse("2026-06-12T10:00:00Z");

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @InjectMocks
    private GetActivityImpactUseCase useCase;

    @Test
    @DisplayName("Calcula el impacto en base a las métricas reales cuando hay estados siguientes")
    void executeShouldReturnImpactStatsBasedOnMetrics() {
        // --- arrange ---
        givenActivityWithImprovingMetrics();
        // --- act ---
        List<ActivityImpactStats> result = calculateImpact();
        // --- assert ---
        thenImpactIs(result, "BREATHING", true, 0.6, -0.5);
    }

    @Test
    @DisplayName("Cae a la configuración por defecto cuando no hay métricas disponibles")
    void executeShouldFallbackToDefaultConfigWhenNoMetricsAvailable() {
        // --- arrange ---
        givenActivityWithoutMetrics();
        // --- act ---
        List<ActivityImpactStats> result = calculateImpact();
        // --- assert ---
        thenImpactIs(result, "BREATHING", false, 0.3, -0.1);
    }

    @Test
    @DisplayName("Ignora eventos sin estado siguiente y trata como cero los deltas con valores nulos")
    void executeShouldIgnoreEventsWithoutNextStateAndNullDeltas() {
        // --- arrange ---
        givenPartialMetricsWithNullDeltasAndMissingNext();
        // --- act ---
        List<ActivityImpactStats> result = calculateImpact();
        // --- assert ---
        thenImpactIs(result, "BREATHING", true, 0.0, 0.0);
    }

    @Test
    @DisplayName("Trata como cero los deltas cuando el siguiente no tiene valencia y el actual no tiene arousal")
    void executeShouldTreatDeltasAsZeroWhenNextValenceAndCurrentArousalAreNull() {
        // --- arrange ---
        givenMetricsWithNullNextValenceAndNullCurrentArousal();
        // --- act ---
        List<ActivityImpactStats> result = calculateImpact();
        // --- assert ---
        thenImpactIs(result, "BREATHING", true, 0.0, 0.0);
    }

    // --- arrange ---

    private void givenActivityWithImprovingMetrics() {
        givenActivities(activityWithEffects(1L, BREATHING, 0.5, -0.2));
        EmotionalEvent recommendation = event(10L, 100L, 1L, 0.0, 0.8, BASE);
        EmotionalEvent nextState = event(11L, 100L, null, 0.6, 0.3, BASE.plusSeconds(300));
        givenRecommendationEvents(recommendation);
        givenUserTimeline(List.of(100L), recommendation, nextState);
    }

    private void givenActivityWithoutMetrics() {
        givenActivities(activityWithEffects(1L, BREATHING, 0.3, -0.1));
        givenRecommendationEvents();
    }

    private void givenPartialMetricsWithNullDeltasAndMissingNext() {
        givenActivities(activityWithEffects(1L, BREATHING, 0.5, -0.2));
        EmotionalEvent acceptedNoNext = event(20L, 200L, 1L, 0.0, 0.0, BASE);
        EmotionalEvent nullValenceCurrent = event(21L, 100L, 1L, null, 0.5, BASE);
        EmotionalEvent orphan = event(22L, null, null, 0.0, 0.0, BASE);
        EmotionalEvent nextForUser100 = event(23L, 100L, null, 0.4, null, BASE.plusSeconds(300));
        givenRecommendationEvents(acceptedNoNext, nullValenceCurrent, orphan);
        givenUserTimeline(List.of(200L, 100L), acceptedNoNext, nullValenceCurrent, nextForUser100);
    }

    private void givenMetricsWithNullNextValenceAndNullCurrentArousal() {
        givenActivities(activityWithEffects(1L, BREATHING, 0.5, -0.2));
        EmotionalEvent current = event(30L, 300L, 1L, 0.2, null, BASE);
        EmotionalEvent next = event(31L, 300L, null, null, 0.7, BASE.plusSeconds(300));
        givenRecommendationEvents(current);
        givenUserTimeline(List.of(300L), current, next);
    }

    private void givenActivities(Activity... activities) {
        when(activityRepository.findAll()).thenReturn(List.of(activities));
    }

    private void givenRecommendationEvents(EmotionalEvent... events) {
        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(events));
    }

    private void givenUserTimeline(List<Long> userIds, EmotionalEvent... timeline) {
        when(emotionalEventRepository.findByUserIds(userIds)).thenReturn(List.of(timeline));
    }

    private Activity activityWithEffects(long id, ActivityType type, double effectValence, double effectArousal) {
        return Activity.builder()
                .id(id)
                .type(type)
                .title(type.name())
                .effectValence(effectValence)
                .effectArousal(effectArousal)
                .build();
    }

    private EmotionalEvent event(long id, Long userId, Long recommendedActivityId,
                                 Double valence, Double arousal, Instant createdAt) {
        return EmotionalEvent.builder()
                .id(id)
                .userId(userId)
                .recommendedActivityId(recommendedActivityId)
                .valence(valence)
                .arousal(arousal)
                .createdAt(createdAt)
                .build();
    }

    // --- act ---

    private List<ActivityImpactStats> calculateImpact() {
        return useCase.execute(Timeframe.MONTH);
    }

    // --- assert ---

    private void thenImpactIs(List<ActivityImpactStats> result, String activityType, boolean basedOnMetrics,
                              double averageValenceChange, double averageArousalChange) {
        assertThat(result).hasSize(1);
        ActivityImpactStats stats = result.get(0);
        assertThat(stats.getActivityType()).isEqualTo(activityType);
        assertThat(stats.isBasedOnMetrics()).isEqualTo(basedOnMetrics);
        assertThat(stats.getAverageValenceChange()).isEqualTo(averageValenceChange);
        assertThat(stats.getAverageArousalChange()).isEqualTo(averageArousalChange);
    }
}
