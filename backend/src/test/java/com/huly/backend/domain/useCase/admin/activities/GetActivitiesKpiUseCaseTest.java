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
import static com.huly.backend.domain.model.enums.RecommendationDecision.ACCEPTED;
import static com.huly.backend.domain.model.enums.RecommendationDecision.IGNORED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActivitiesKpiUseCaseTest {

    private static final Instant BASE = Instant.parse("2026-06-12T10:00:00Z");

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @Mock
    private ActivitySessionRepository activitySessionRepository;

    @InjectMocks
    private GetActivitiesKpiUseCase useCase;

    @Test
    @DisplayName("Calcula los KPIs y una mejora del 100% cuando la valencia mejora tras la recomendación aceptada")
    void executeShouldCalculateKpisCorrectly() {
        // --- arrange ---
        givenAcceptedRecommendationWithImprovedValence();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 2, "BREATHING", 2, 100.0);
    }

    @Test
    @DisplayName("Devuelve KPIs en cero cuando no hay sesiones ni eventos")
    void executeShouldReturnZeroKpisWhenNoData() {
        // --- arrange ---
        givenNoActivityData();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 0, null, 0, 0.0);
    }

    @Test
    @DisplayName("Reporta mejora en cero cuando la valencia del estado siguiente no mejora")
    void executeShouldReportZeroImprovementWhenValenceDoesNotImprove() {
        // --- arrange ---
        givenAcceptedRecommendationWithoutValenceImprovement();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 1, "BREATHING", 1, 0.0);
    }

    @Test
    @DisplayName("Ignora recomendaciones no aceptadas, sin estado siguiente y con datos nulos")
    void executeShouldIgnoreNonAcceptedAndEventsWithoutNextState() {
        // --- arrange ---
        givenNonImprovingRecommendations();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 1, "BREATHING", 1, 0.0);
    }

    @Test
    @DisplayName("No cuenta como mejora cuando falta la valencia del estado siguiente")
    void executeShouldReportZeroImprovementWhenNextStateValenceIsMissing() {
        // --- arrange ---
        givenAcceptedRecommendationWithMissingNextValence();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 1, "BREATHING", 1, 0.0);
    }

    @Test
    @DisplayName("No cuenta como mejora cuando falta la valencia del estado actual aceptado")
    void executeShouldReportZeroImprovementWhenCurrentStateValenceIsMissing() {
        // --- arrange ---
        givenAcceptedRecommendationWithMissingCurrentValence();
        // --- act ---
        ActivitiesKpiStats result = calculateKpi();
        // --- assert ---
        thenKpiIs(result, 1, "BREATHING", 1, 0.0);
    }

    // Los ternarios `topEntry != null ? ... : ...` en buildKpiStats son código defensivo inalcanzable:
    // solo se evalúan cuando hay sesiones, por lo que sessionCounts nunca está vacío y max() siempre
    // devuelve una entrada; topEntry no puede ser nulo en ese punto.

    // --- arrange ---

    private void givenAcceptedRecommendationWithImprovedValence() {
        givenActivities(activity(1L, BREATHING));
        givenSessions(session(BREATHING), session(BREATHING));
        EmotionalEvent recommendation = event(10L, 100L, 1L, ACCEPTED, 0.0, BASE);
        EmotionalEvent nextState = event(11L, 100L, null, null, 0.5, BASE.plusSeconds(300));
        givenRecommendationEvents(recommendation);
        givenUserTimeline(List.of(100L), recommendation, nextState);
    }

    private void givenNoActivityData() {
        givenActivities();
        givenSessions();
        givenRecommendationEvents();
    }

    private void givenAcceptedRecommendationWithoutValenceImprovement() {
        givenActivities(activity(1L, BREATHING));
        givenSessions(session(BREATHING));
        EmotionalEvent recommendation = event(20L, 100L, 1L, ACCEPTED, 0.5, BASE);
        EmotionalEvent nextState = event(21L, 100L, null, null, 0.5, BASE.plusSeconds(300));
        givenRecommendationEvents(recommendation);
        givenUserTimeline(List.of(100L), recommendation, nextState);
    }

    private void givenNonImprovingRecommendations() {
        givenActivities(activity(1L, BREATHING), activity(2L, DIARY));
        givenSessions(session(BREATHING));
        EmotionalEvent ignored = event(30L, 100L, 1L, IGNORED, 0.2, BASE);
        EmotionalEvent acceptedNoNext = event(31L, 200L, 1L, ACCEPTED, 0.2, BASE);
        EmotionalEvent orphan = event(32L, null, null, null, 0.2, BASE);
        givenRecommendationEvents(ignored, acceptedNoNext, orphan);
        givenUserTimeline(List.of(100L, 200L), ignored, acceptedNoNext);
    }

    private void givenAcceptedRecommendationWithMissingNextValence() {
        givenActivities(activity(1L, BREATHING));
        givenSessions(session(BREATHING));
        EmotionalEvent recommendation = event(40L, 100L, 1L, ACCEPTED, 0.3, BASE);
        EmotionalEvent nextState = event(41L, 100L, null, null, null, BASE.plusSeconds(300));
        givenRecommendationEvents(recommendation);
        givenUserTimeline(List.of(100L), recommendation, nextState);
    }

    private void givenAcceptedRecommendationWithMissingCurrentValence() {
        givenActivities(activity(1L, BREATHING));
        givenSessions(session(BREATHING));
        EmotionalEvent recommendation = event(50L, 100L, 1L, ACCEPTED, null, BASE);
        EmotionalEvent nextState = event(51L, 100L, null, null, 0.5, BASE.plusSeconds(300));
        givenRecommendationEvents(recommendation);
        givenUserTimeline(List.of(100L), recommendation, nextState);
    }

    private void givenActivities(Activity... activities) {
        when(activityRepository.findAll()).thenReturn(List.of(activities));
    }

    private void givenSessions(ActivitySession... sessions) {
        when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of(sessions));
    }

    private void givenRecommendationEvents(EmotionalEvent... events) {
        when(emotionalEventRepository.findAllRecommendationEventsAfter(any())).thenReturn(List.of(events));
    }

    private void givenUserTimeline(List<Long> userIds, EmotionalEvent... timeline) {
        when(emotionalEventRepository.findByUserIds(userIds)).thenReturn(List.of(timeline));
    }

    private Activity activity(long id, ActivityType type) {
        return Activity.builder().id(id).type(type).title(type.name()).build();
    }

    private ActivitySession session(ActivityType type) {
        return ActivitySession.builder().activityType(type).build();
    }

    private EmotionalEvent event(long id, Long userId, Long recommendedActivityId,
                                 RecommendationDecision decision, Double valence, Instant createdAt) {
        return EmotionalEvent.builder()
                .id(id)
                .userId(userId)
                .recommendedActivityId(recommendedActivityId)
                .recommendationDecision(decision)
                .valence(valence)
                .createdAt(createdAt)
                .build();
    }

    // --- act ---

    private ActivitiesKpiStats calculateKpi() {
        return useCase.execute(Timeframe.MONTH);
    }

    // --- assert ---

    private void thenKpiIs(ActivitiesKpiStats result, long totalSessions, String topActivityType,
                           long topActivitySessions, double averageMoodImprovement) {
        assertThat(result.getTotalSessions()).isEqualTo(totalSessions);
        assertThat(result.getTopActivityType()).isEqualTo(topActivityType);
        assertThat(result.getTopActivitySessions()).isEqualTo(topActivitySessions);
        assertThat(result.getAverageMoodImprovement()).isEqualTo(averageMoodImprovement);
    }
}
