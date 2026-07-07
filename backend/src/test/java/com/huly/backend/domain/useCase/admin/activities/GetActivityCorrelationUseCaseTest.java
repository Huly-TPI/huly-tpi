package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityCorrelationStats;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.huly.backend.domain.model.enums.ActivityType.BREATHING;
import static com.huly.backend.domain.model.enums.ActivityType.DIARY;
import static com.huly.backend.domain.model.enums.RecommendationDecision.ACCEPTED;
import static com.huly.backend.domain.model.enums.RecommendationDecision.IGNORED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActivityCorrelationUseCaseTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @InjectMocks
    private GetActivityCorrelationUseCase useCase;

    @Test
    @DisplayName("Calcula la tasa de aceptación por emoción para una actividad")
    void executeShouldCalculateCorrelationStatsCorrectly() {
        // --- arrange ---
        givenBreathingWithAnxietyEvents();
        // --- act ---
        List<ActivityCorrelationStats> result = calculateCorrelation();
        // --- assert ---
        thenStatsAreNotEmpty(result);
        thenCorrelationIs(result, "Ansiedad", "BREATHING", 2, 50.0);
    }

    @Test
    @DisplayName("Ignora eventos sin actividad o sin emoción y usa lista vacía para actividades sin datos")
    void executeShouldIgnoreEventsWithoutActivityOrEmotionAndDefaultEmptyActivities() {
        // --- arrange ---
        givenTwoActivitiesWithFilterableEvents();
        // --- act ---
        List<ActivityCorrelationStats> result = calculateCorrelation();
        // --- assert ---
        thenStatsAreNotEmpty(result);
        thenCorrelationIs(result, "Ansiedad", "BREATHING", 1, 100.0);
        thenCorrelationIs(result, "Ansiedad", "DIARY", 0, 0.0);
    }

    // --- arrange ---

    private void givenBreathingWithAnxietyEvents() {
        givenActivities(activity(1L, BREATHING));
        givenRecommendationEvents(
                correlationEvent(1L, "ANXIETY", ACCEPTED),
                correlationEvent(1L, "ANXIETY", IGNORED));
    }

    private void givenTwoActivitiesWithFilterableEvents() {
        givenActivities(activity(1L, BREATHING), activity(2L, DIARY));
        givenRecommendationEvents(
                correlationEvent(1L, "ANXIETY", ACCEPTED),
                correlationEvent(null, "ANXIETY", ACCEPTED),
                correlationEvent(1L, null, ACCEPTED));
    }

    private void givenActivities(Activity... activities) {
        when(activityRepository.findAll()).thenReturn(List.of(activities));
    }

    private void givenRecommendationEvents(EmotionalEvent... events) {
        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(events));
    }

    private Activity activity(long id, ActivityType type) {
        return Activity.builder().id(id).type(type).title(type.name()).build();
    }

    private EmotionalEvent correlationEvent(Long recommendedActivityId, String detectedEmotion,
                                            RecommendationDecision decision) {
        return EmotionalEvent.builder()
                .recommendedActivityId(recommendedActivityId)
                .detectedEmotion(detectedEmotion)
                .recommendationDecision(decision)
                .build();
    }

    // --- act ---

    private List<ActivityCorrelationStats> calculateCorrelation() {
        return useCase.execute(Timeframe.MONTH);
    }

    // --- assert ---

    private void thenStatsAreNotEmpty(List<ActivityCorrelationStats> result) {
        assertThat(result).isNotEmpty();
    }

    private void thenCorrelationIs(List<ActivityCorrelationStats> result, String emotion,
                                   String activityType, long suggestionsCount, double acceptanceRate) {
        ActivityCorrelationStats stats = findStats(result, emotion, activityType);
        assertThat(stats.getActivityType()).isEqualTo(activityType);
        assertThat(stats.getSuggestionsCount()).isEqualTo(suggestionsCount);
        assertThat(stats.getAcceptanceRate()).isEqualTo(acceptanceRate);
    }

    private ActivityCorrelationStats findStats(List<ActivityCorrelationStats> result, String emotion,
                                               String activityType) {
        return result.stream()
                .filter(s -> s.getEmotion().equalsIgnoreCase(emotion) && s.getActivityType().equals(activityType))
                .findFirst()
                .orElseThrow();
    }
}
