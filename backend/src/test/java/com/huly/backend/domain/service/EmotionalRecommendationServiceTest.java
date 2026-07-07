package com.huly.backend.domain.service;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalRecommendationServiceTest {

    private final EmotionalRecommendationService service = new EmotionalRecommendationService();

    private EmotionalRecommendation query;
    private List<Activity> activities;
    private List<EmotionalEvent> history;

    @Test
    @DisplayName("Recomienda respiración primero ante ansiedad con arousal alto")
    void recommendShouldRankBreathingFirstForAnxietyWithHighArousal() {
        givenQuery(query(-0.8, 0.9, -0.7, 0.85, "calmarme para dormir"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult result = recommend();

        thenNotFallback(result);
        thenRecommendationsNotEmpty(result);
        thenTopType(result, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Sugiere diario o farolitos ante valencia baja con arousal medio")
    void recommendShouldSuggestJournalOrLanternForLowValenceWithMediumArousal() {
        givenQuery(query(-0.8, 0.2, -0.1, 0.6, "quiero reflexionar y ordenar pensamientos"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult result = recommend();

        thenNotFallback(result);
        thenTopTypeIn(result, ActivityType.DIARY, ActivityType.LANTERN);
    }

    @Test
    @DisplayName("Usa fallback cuando ninguna actividad coincide con los rangos VAD")
    void recommendShouldUseFallbackWhenNoActivityMatchesVadRanges() {
        givenQuery(query(0.9, 0.9, 0.9, 0.8, "calmarme"));
        givenActivities(outOfRangeActivities());

        EmotionalRecommendationResult result = recommend();

        thenFallbackUsed(result);
        thenRecommendationCount(result, 2);
        thenReasonContains(result, ActivityType.DIARY, "Fallback");
    }

    @Test
    @DisplayName("Ordena las recomendaciones por score descendente")
    void recommendShouldOrderByScoreDescending() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult result = recommend();

        thenSortedByScoreDescending(result);
    }

    @Test
    @DisplayName("Usa el objetivo del usuario para influir en el ranking")
    void recommendShouldUseUserGoalToInfluenceRanking() {
        givenQuery(query(-0.7, 0.1, -0.1, 0.55, "necesito escribir y ordenar pensamientos"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult result = recommend();

        thenTopType(result, ActivityType.DIARY);
    }

    @Test
    @DisplayName("Mantiene el ranking VAD cuando el historial del usuario está vacío")
    void recommendShouldKeepVadRankingWhenUserHistoryIsEmpty() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult withoutHistory = recommend();
        givenHistory(emptyHistory());
        EmotionalRecommendationResult withEmptyHistory = recommendWithHistory();

        thenSameTypesAndScores(withEmptyHistory, withoutHistory);
    }

    @Test
    @DisplayName("Se comporta como sin historial cuando el historial es null")
    void recommendShouldBehaveLikeNoHistoryWhenHistoryIsNull() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult withoutHistory = recommend();
        givenHistory(null);
        EmotionalRecommendationResult withNullHistory = recommendWithHistory();

        thenSameTypesAndScores(withNullHistory, withoutHistory);
    }

    @Test
    @DisplayName("Sube la actividad con historial aceptado positivo")
    void recommendShouldRaiseActivityWithPositiveAcceptedHistory() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(RecommendationDecision.ACCEPTED, 2L, 2L, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenScoreHigher(result, base, ActivityType.DIARY);
    }

    @Test
    @DisplayName("Baja la actividad con historial ignorado")
    void recommendShouldLowerActivityWithIgnoredHistory() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(RecommendationDecision.IGNORED, 1L, null, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenScoreLower(result, base, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Usa la actividad elegida como señal positiva cuando el usuario eligió otra")
    void recommendShouldUseChosenActivityAsPositiveSignalWhenUserChoseOther() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(RecommendationDecision.CHOSE_OTHER, 1L, 2L, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenScoreHigher(result, base, ActivityType.DIARY);
        thenScoreLower(result, base, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Sube con feedback alto y baja con feedback bajo")
    void recommendShouldRaiseHighFeedbackAndLowerLowFeedback() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(null, 2L, null, 5)));
        EmotionalRecommendationResult highFeedback = recommendWithHistory();
        givenHistory(historyOf(event(null, 2L, null, 1)));
        EmotionalRecommendationResult lowFeedback = recommendWithHistory();

        thenScoreHigher(highFeedback, base, ActivityType.DIARY);
        thenScoreLower(lowFeedback, base, ActivityType.DIARY);
    }

    @Test
    @DisplayName("Mantiene el ajuste de tendencia acotado para que el VAD siga importando")
    void recommendShouldKeepTrendAdjustmentBoundedSoVadStillMatters() {
        givenQuery(query(-0.8, 0.9, -0.7, 0.85, "calmarme para dormir"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(
                event(RecommendationDecision.ACCEPTED, 2L, 2L, 5),
                event(RecommendationDecision.ACCEPTED, 2L, 2L, 5),
                event(RecommendationDecision.ACCEPTED, 2L, 2L, 5)
        ));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenTopType(result, ActivityType.BREATHING);
        thenTrendDifferenceAtMost(result, base, ActivityType.DIARY, 0.15);
    }

    @Test
    @DisplayName("Devuelve resultado vacío cuando la lista de actividades es null")
    void recommendShouldReturnEmptyWhenActivitiesAreNull() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(null);

        EmotionalRecommendationResult result = recommend();

        thenEmptyResult(result);
    }

    @Test
    @DisplayName("Devuelve resultado vacío cuando la lista de actividades está vacía")
    void recommendShouldReturnEmptyWhenActivitiesAreEmpty() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(emptyActivities());

        EmotionalRecommendationResult result = recommend();

        thenEmptyResult(result);
    }

    @Test
    @DisplayName("Usa el efecto general cuando no aplica ningún factor VAD")
    void recommendShouldUseGeneralEffectWhenNoVadFactorsApply() {
        givenQuery(query(0.0, 0.4, 0.0, 0.8, null));
        givenActivities(generalEffectActivities());

        EmotionalRecommendationResult result = recommend();

        thenNotFallback(result);
        thenRecommendationCount(result, 2);
        thenSortedByScoreDescending(result);
        thenAllScoresPositive(result);
    }

    @Test
    @DisplayName("Puntúa más alto cuando el objetivo coincide con las palabras clave")
    void recommendShouldScoreGoalKeywordMatchHigherThanNonMatch() {
        givenActivities(keywordActivities("dormir,calma"));

        givenQuery(query(0.0, 0.4, 0.0, 0.5, "quiero calma"));
        EmotionalRecommendationResult matching = recommend();
        givenQuery(query(0.0, 0.4, 0.0, 0.5, "correr rapido"));
        EmotionalRecommendationResult nonMatching = recommend();

        thenScoreHigher(matching, nonMatching, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Ignora el objetivo cuando el objetivo del usuario es null")
    void recommendShouldIgnoreGoalWhenUserGoalIsNull() {
        givenActivities(keywordActivities("dormir,calma"));

        givenQuery(query(0.0, 0.4, 0.0, 0.5, null));
        EmotionalRecommendationResult nullGoal = recommend();
        givenQuery(query(0.0, 0.4, 0.0, 0.5, "correr rapido"));
        EmotionalRecommendationResult nonMatching = recommend();

        thenScoreEqual(nullGoal, nonMatching, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Ignora el objetivo cuando las palabras clave están en blanco")
    void recommendShouldIgnoreGoalWhenKeywordsAreBlank() {
        givenActivities(keywordActivities("   "));

        givenQuery(query(0.0, 0.4, 0.0, 0.5, "calma"));
        EmotionalRecommendationResult goalLikeMatch = recommend();
        givenQuery(query(0.0, 0.4, 0.0, 0.5, "correr rapido"));
        EmotionalRecommendationResult otherGoal = recommend();

        thenScoreEqual(goalLikeMatch, otherGoal, ActivityType.BREATHING);
    }

    @Test
    @DisplayName("Aplica intensidad y razones por tipo de actividad con arousal alto")
    void recommendShouldApplyIntensityAndReasonsForHighArousalTypes() {
        givenQuery(query(-0.8, 0.9, 0.0, 0.8, null));
        givenActivities(allTypeActivities());

        EmotionalRecommendationResult result = recommend();

        thenNotFallback(result);
        thenRecommendationCount(result, 7);
        thenSortedByScoreDescending(result);
        thenReasonContains(result, ActivityType.BREATHING, "arousal es alto");
        thenReasonContains(result, ActivityType.DIARY, "ordenar pensamientos");
        thenReasonContains(result, ActivityType.LANTERN, "soltar pensamientos");
        thenReasonContains(result, ActivityType.ZEN_GARDEN, "arena");
        thenReasonContains(result, ActivityType.MANDALA, "formas complejas");
        thenReasonContains(result, ActivityType.CHALLENGE, "reto");
        thenReasonContains(result, ActivityType.BUBBLE, "compatibilidad");
    }

    @Test
    @DisplayName("Aplica intensidad y razón de respiración por tipo con arousal bajo")
    void recommendShouldApplyIntensityAndReasonsForLowArousalTypes() {
        givenQuery(query(0.5, 0.1, 0.5, 0.8, null));
        givenActivities(allTypeActivities());

        EmotionalRecommendationResult result = recommend();

        thenNotFallback(result);
        thenRecommendationCount(result, 7);
        thenSortedByScoreDescending(result);
        thenReasonContains(result, ActivityType.BREATHING, "compatibilidad");
    }

    @Test
    @DisplayName("Ignora los eventos null del historial")
    void recommendShouldIgnoreNullEventsInHistory() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyWithLeadingNull(event(RecommendationDecision.ACCEPTED, 2L, 2L, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenScoreHigher(result, base, ActivityType.DIARY);
    }

    @Test
    @DisplayName("Ignora el feedback neutro sin ajustar el score")
    void recommendShouldIgnoreNeutralFeedbackScore() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(null, 2L, null, 3)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenSameTypesAndScores(result, base);
    }

    @Test
    @DisplayName("Ignora un feedback desconocido sin ajustar el score")
    void recommendShouldIgnoreUnknownFeedbackScore() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(null, 2L, null, 0)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenSameTypesAndScores(result, base);
    }

    @Test
    @DisplayName("Ignora el historial que referencia una actividad desconocida")
    void recommendShouldIgnoreHistoryReferencingUnknownActivity() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(defaultActivities());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(RecommendationDecision.ACCEPTED, 999L, 999L, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenSameTypesAndScores(result, base);
    }

    @Test
    @DisplayName("Omite las actividades sin id al construir las tendencias")
    void recommendShouldSkipActivitiesWithNullIdWhenBuildingTrends() {
        givenQuery(query(-0.7, 0.8, -0.6, 0.8, "calmarme"));
        givenActivities(activitiesWithNullId());

        EmotionalRecommendationResult base = recommend();
        givenHistory(historyOf(event(RecommendationDecision.ACCEPTED, 1L, 1L, null)));
        EmotionalRecommendationResult result = recommendWithHistory();

        thenRecommendationCount(result, 2);
        thenScoreHigher(result, base, ActivityType.BREATHING);
    }

    // --- arrange ---
    private void givenQuery(EmotionalRecommendation value) {
        this.query = value;
    }

    private void givenActivities(List<Activity> value) {
        this.activities = value;
    }

    private void givenHistory(List<EmotionalEvent> value) {
        this.history = value;
    }

    private EmotionalRecommendation query(
            double valence,
            double arousal,
            double dominance,
            double intensity,
            String userGoal
    ) {
        return new EmotionalRecommendation(
                new Vad(valence, arousal, dominance),
                intensity,
                userGoal
        );
    }

    private EmotionalEvent event(
            RecommendationDecision decision,
            Long recommendedActivityId,
            Long chosenActivityId,
            Integer feedbackScore
    ) {
        return EmotionalEvent.builder()
                .userId(1L)
                .recommendedActivityId(recommendedActivityId)
                .chosenActivityId(chosenActivityId)
                .recommendationDecision(decision)
                .feedbackScore(feedbackScore)
                .createdAt(Instant.now())
                .build();
    }

    private List<EmotionalEvent> historyOf(EmotionalEvent... events) {
        return List.of(events);
    }

    private List<EmotionalEvent> historyWithLeadingNull(EmotionalEvent event) {
        List<EmotionalEvent> result = new ArrayList<>();
        result.add(null);
        result.add(event);
        return result;
    }

    private List<EmotionalEvent> emptyHistory() {
        return List.of();
    }

    private List<Activity> emptyActivities() {
        return List.of();
    }

    private List<Activity> defaultActivities() {
        return List.of(
                activity(1L, ActivityType.BREATHING, -1.0, 0.3, 0.6, 1.0, -1.0, 1.0, 0.30, -0.30, 0.10),
                activity(2L, ActivityType.DIARY, -1.0, 0.0, -0.5, 0.5, -0.5, 1.0, 0.35, -0.10, 0.20),
                activity(3L, ActivityType.LANTERN, -1.0, 0.2, -0.3, 1.0, -1.0, 1.0, 0.20, -0.20, 0.10),
                activity(4L, ActivityType.BUBBLE, -1.0, 0.3, -1.0, 1.0, -1.0, 0.5, 0.25, 0.05, 0.10)
        );
    }

    private List<Activity> outOfRangeActivities() {
        return List.of(
                activity(1L, ActivityType.BREATHING, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.3, -0.3, 0.1),
                activity(2L, ActivityType.DIARY, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.35, -0.1, 0.2)
        );
    }

    private List<Activity> generalEffectActivities() {
        return List.of(
                activity(1L, ActivityType.BREATHING, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -0.10, -0.20, 0.20),
                activity(2L, ActivityType.DIARY, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.30, 0.40, -0.10)
        );
    }

    private List<Activity> allTypeActivities() {
        return List.of(
                wideActivity(1L, ActivityType.BREATHING),
                wideActivity(2L, ActivityType.DIARY),
                wideActivity(3L, ActivityType.LANTERN),
                wideActivity(4L, ActivityType.BUBBLE),
                wideActivity(5L, ActivityType.CHALLENGE),
                wideActivity(6L, ActivityType.ZEN_GARDEN),
                wideActivity(7L, ActivityType.MANDALA)
        );
    }

    private List<Activity> activitiesWithNullId() {
        return List.of(
                wideActivity(1L, ActivityType.BREATHING),
                wideActivity(null, ActivityType.DIARY)
        );
    }

    private List<Activity> keywordActivities(String goalKeywords) {
        return List.of(
                Activity.builder()
                        .id(1L)
                        .type(ActivityType.BREATHING)
                        .valenceMin(-1.0).valenceMax(1.0)
                        .arousalMin(-1.0).arousalMax(1.0)
                        .dominanceMin(-1.0).dominanceMax(1.0)
                        .effectValence(0.20).effectArousal(-0.10).effectDominance(0.10)
                        .goalKeywords(goalKeywords)
                        .build()
        );
    }

    private Activity wideActivity(Long id, ActivityType type) {
        return activity(id, type, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.20, -0.20, 0.10);
    }

    private Activity activity(
            Long id,
            ActivityType type,
            double valenceMin,
            double valenceMax,
            double arousalMin,
            double arousalMax,
            double dominanceMin,
            double dominanceMax,
            double effectValence,
            double effectArousal,
            double effectDominance
    ) {
        return Activity.builder()
                .id(id)
                .type(type)
                .valenceMin(valenceMin)
                .valenceMax(valenceMax)
                .arousalMin(arousalMin)
                .arousalMax(arousalMax)
                .dominanceMin(dominanceMin)
                .dominanceMax(dominanceMax)
                .effectValence(effectValence)
                .effectArousal(effectArousal)
                .effectDominance(effectDominance)
                .build();
    }

    // --- act ---
    private EmotionalRecommendationResult recommend() {
        return service.recommend(query, activities);
    }

    private EmotionalRecommendationResult recommendWithHistory() {
        return service.recommend(query, activities, history);
    }

    // --- assert ---
    private void thenNotFallback(EmotionalRecommendationResult result) {
        assertThat(result.fallbackUsed()).isFalse();
    }

    private void thenFallbackUsed(EmotionalRecommendationResult result) {
        assertThat(result.fallbackUsed()).isTrue();
    }

    private void thenRecommendationsNotEmpty(EmotionalRecommendationResult result) {
        assertThat(result.recommendations()).isNotEmpty();
    }

    private void thenRecommendationCount(EmotionalRecommendationResult result, int expected) {
        assertThat(result.recommendations()).hasSize(expected);
    }

    private void thenEmptyResult(EmotionalRecommendationResult result) {
        assertThat(result.recommendations()).isEmpty();
        assertThat(result.fallbackUsed()).isFalse();
    }

    private void thenTopType(EmotionalRecommendationResult result, ActivityType type) {
        assertThat(result.recommendations().get(0).type()).isEqualTo(type);
    }

    private void thenTopTypeIn(EmotionalRecommendationResult result, ActivityType first, ActivityType second) {
        assertThat(result.recommendations().get(0).type()).isIn(first, second);
    }

    private void thenSortedByScoreDescending(EmotionalRecommendationResult result) {
        assertThat(result.recommendations())
                .extracting("score", Double.class)
                .isSortedAccordingTo((left, right) -> Double.compare(right, left));
    }

    private void thenAllScoresPositive(EmotionalRecommendationResult result) {
        assertThat(result.recommendations()).allMatch(recommendation -> recommendation.score() > 0.0);
    }

    private void thenSameTypesAndScores(
            EmotionalRecommendationResult result,
            EmotionalRecommendationResult expected
    ) {
        assertThat(result.recommendations())
                .extracting("type", ActivityType.class)
                .containsExactlyElementsOf(expected.recommendations().stream()
                        .map(recommendation -> recommendation.type())
                        .toList());
        assertThat(result.recommendations())
                .extracting("score", Double.class)
                .containsExactlyElementsOf(expected.recommendations().stream()
                        .map(recommendation -> recommendation.score())
                        .toList());
    }

    private void thenScoreHigher(
            EmotionalRecommendationResult result,
            EmotionalRecommendationResult base,
            ActivityType type
    ) {
        assertThat(scoreFor(result, type)).isGreaterThan(scoreFor(base, type));
    }

    private void thenScoreLower(
            EmotionalRecommendationResult result,
            EmotionalRecommendationResult base,
            ActivityType type
    ) {
        assertThat(scoreFor(result, type)).isLessThan(scoreFor(base, type));
    }

    private void thenScoreEqual(
            EmotionalRecommendationResult result,
            EmotionalRecommendationResult base,
            ActivityType type
    ) {
        assertThat(scoreFor(result, type)).isEqualTo(scoreFor(base, type));
    }

    private void thenTrendDifferenceAtMost(
            EmotionalRecommendationResult result,
            EmotionalRecommendationResult base,
            ActivityType type,
            double maxDifference
    ) {
        assertThat(scoreFor(result, type) - scoreFor(base, type)).isLessThanOrEqualTo(maxDifference);
    }

    private void thenReasonContains(EmotionalRecommendationResult result, ActivityType type, String fragment) {
        assertThat(reasonFor(result, type)).contains(fragment);
    }

    private double scoreFor(EmotionalRecommendationResult result, ActivityType type) {
        return itemFor(result, type).score();
    }

    private String reasonFor(EmotionalRecommendationResult result, ActivityType type) {
        return itemFor(result, type).reason();
    }

    private EmotionalRecommendationItem itemFor(EmotionalRecommendationResult result, ActivityType type) {
        return result.recommendations().stream()
                .filter(recommendation -> recommendation.type() == type)
                .findFirst()
                .orElseThrow();
    }
}
