package com.huly.backend.domain.service;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalRecommendationServiceTest {

    private final EmotionalRecommendationService service = new EmotionalRecommendationService();

    @Test
    void recommend_shouldRankBreathingFirstForAnxietyWithHighArousal() {
        EmotionalRecommendationResult result = service.recommend(
                query(-0.8, 0.9, -0.7, 0.85, "calmarme para dormir"),
                defaultActivities()
        );

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.recommendations()).isNotEmpty();
        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.BREATHING);
    }

    @Test
    void recommend_shouldSuggestJournalOrCloudForLowValenceWithMediumArousal() {
        EmotionalRecommendationResult result = service.recommend(
                query(-0.8, 0.2, -0.1, 0.6, "quiero reflexionar y ordenar pensamientos"),
                defaultActivities()
        );

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.recommendations().get(0).type())
                .isIn(ActivityType.DIARY, ActivityType.LANTERN);
    }

    @Test
    void recommend_shouldUseFallbackWhenNoActivityMatchesVadRanges() {
        List<Activity> activities = List.of(
                activity(1L, ActivityType.BREATHING, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.3, -0.3, 0.1),
                activity(2L, ActivityType.DIARY, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.35, -0.1, 0.2)
        );

        EmotionalRecommendationResult result = service.recommend(
                query(0.9, 0.9, 0.9, 0.8, "calmarme"),
                activities
        );

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.recommendations()).hasSize(2);
    }

    @Test
    void recommend_shouldOrderByScoreDescending() {
        EmotionalRecommendationResult result = service.recommend(
                query(-0.7, 0.8, -0.6, 0.8, "calmarme"),
                defaultActivities()
        );

        assertThat(result.recommendations())
                .extracting("score", Double.class)
                .isSortedAccordingTo((left, right) -> Double.compare(right, left));
    }

    @Test
    void recommend_shouldUseUserGoalToInfluenceRanking() {
        EmotionalRecommendationResult result = service.recommend(
                query(-0.7, 0.1, -0.1, 0.55, "necesito escribir y ordenar pensamientos"),
                defaultActivities()
        );

        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.DIARY);
    }

    @Test
    void recommend_shouldKeepVadRanking_whenUserHistoryIsEmpty() {
        EmotionalRecommendation query = query(-0.7, 0.8, -0.6, 0.8, "calmarme");
        List<Activity> activities = defaultActivities();

        EmotionalRecommendationResult withoutHistory = service.recommend(query, activities);
        EmotionalRecommendationResult withEmptyHistory = service.recommend(query, activities, List.of());

        assertThat(withEmptyHistory.recommendations())
                .extracting("type", ActivityType.class)
                .containsExactlyElementsOf(withoutHistory.recommendations().stream()
                        .map(recommendation -> recommendation.type())
                        .toList());
        assertThat(withEmptyHistory.recommendations())
                .extracting("score", Double.class)
                .containsExactlyElementsOf(withoutHistory.recommendations().stream()
                        .map(recommendation -> recommendation.score())
                        .toList());
    }

    @Test
    void recommend_shouldRaiseActivityWithPositiveAcceptedHistory() {
        EmotionalRecommendation query = query(-0.7, 0.8, -0.6, 0.8, "calmarme");
        List<Activity> activities = defaultActivities();

        double baseScore = scoreFor(service.recommend(query, activities), ActivityType.DIARY);
        EmotionalRecommendationResult result = service.recommend(
                query,
                activities,
                List.of(event(RecommendationDecision.ACCEPTED, 2L, 2L, null))
        );

        assertThat(scoreFor(result, ActivityType.DIARY)).isGreaterThan(baseScore);
    }

    @Test
    void recommend_shouldLowerActivityWithIgnoredHistory() {
        EmotionalRecommendation query = query(-0.7, 0.8, -0.6, 0.8, "calmarme");
        List<Activity> activities = defaultActivities();

        double baseScore = scoreFor(service.recommend(query, activities), ActivityType.BREATHING);
        EmotionalRecommendationResult result = service.recommend(
                query,
                activities,
                List.of(event(RecommendationDecision.IGNORED, 1L, null, null))
        );

        assertThat(scoreFor(result, ActivityType.BREATHING)).isLessThan(baseScore);
    }

    @Test
    void recommend_shouldUseChosenActivityAsPositiveSignal_whenUserChoseOther() {
        EmotionalRecommendation query = query(-0.7, 0.8, -0.6, 0.8, "calmarme");
        List<Activity> activities = defaultActivities();

        EmotionalRecommendationResult result = service.recommend(
                query,
                activities,
                List.of(event(RecommendationDecision.CHOSE_OTHER, 1L, 2L, null))
        );

        assertThat(scoreFor(result, ActivityType.DIARY))
                .isGreaterThan(scoreFor(service.recommend(query, activities), ActivityType.DIARY));
        assertThat(scoreFor(result, ActivityType.BREATHING))
                .isLessThan(scoreFor(service.recommend(query, activities), ActivityType.BREATHING));
    }

    @Test
    void recommend_shouldRaiseHighFeedbackAndLowerLowFeedback() {
        EmotionalRecommendation query = query(-0.7, 0.8, -0.6, 0.8, "calmarme");
        List<Activity> activities = defaultActivities();
        EmotionalRecommendationResult base = service.recommend(query, activities);

        EmotionalRecommendationResult highFeedback = service.recommend(
                query,
                activities,
                List.of(event(null, 2L, null, 5))
        );
        EmotionalRecommendationResult lowFeedback = service.recommend(
                query,
                activities,
                List.of(event(null, 2L, null, 1))
        );

        assertThat(scoreFor(highFeedback, ActivityType.DIARY)).isGreaterThan(scoreFor(base, ActivityType.DIARY));
        assertThat(scoreFor(lowFeedback, ActivityType.DIARY)).isLessThan(scoreFor(base, ActivityType.DIARY));
    }

    @Test
    void recommend_shouldKeepTrendAdjustmentBoundedSoVadStillMatters() {
        EmotionalRecommendation query = query(-0.8, 0.9, -0.7, 0.85, "calmarme para dormir");
        List<Activity> activities = defaultActivities();

        EmotionalRecommendationResult result = service.recommend(
                query,
                activities,
                List.of(
                        event(RecommendationDecision.ACCEPTED, 2L, 2L, 5),
                        event(RecommendationDecision.ACCEPTED, 2L, 2L, 5),
                        event(RecommendationDecision.ACCEPTED, 2L, 2L, 5)
                )
        );

        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.BREATHING);
        assertThat(scoreFor(result, ActivityType.DIARY) - scoreFor(service.recommend(query, activities), ActivityType.DIARY))
                .isLessThanOrEqualTo(0.15);
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

    private double scoreFor(EmotionalRecommendationResult result, ActivityType type) {
        return result.recommendations().stream()
                .filter(recommendation -> recommendation.type() == type)
                .findFirst()
                .orElseThrow()
                .score();
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

    private List<Activity> defaultActivities() {
        return List.of(
                activity(1L, ActivityType.BREATHING, -1.0, 0.3, 0.6, 1.0, -1.0, 1.0, 0.30, -0.30, 0.10),
                activity(2L, ActivityType.DIARY, -1.0, 0.0, -0.5, 0.5, -0.5, 1.0, 0.35, -0.10, 0.20),
                activity(3L, ActivityType.LANTERN, -1.0, 0.2, -0.3, 1.0, -1.0, 1.0, 0.20, -0.20, 0.10),
                activity(4L, ActivityType.BUBBLE, -1.0, 0.3, -1.0, 1.0, -1.0, 0.5, 0.25, 0.05, 0.10)
        );
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
}
