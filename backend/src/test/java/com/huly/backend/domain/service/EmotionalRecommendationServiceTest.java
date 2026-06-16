package com.huly.backend.domain.service;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import org.junit.jupiter.api.Test;

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
        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.RESPIRACION);
    }

    @Test
    void recommend_shouldSuggestJournalOrCloudForLowValenceWithMediumArousal() {
        EmotionalRecommendationResult result = service.recommend(
                query(-0.8, 0.2, -0.1, 0.6, "quiero reflexionar y ordenar pensamientos"),
                defaultActivities()
        );

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.recommendations().get(0).type())
                .isIn(ActivityType.DIARIO, ActivityType.NUBE);
    }

    @Test
    void recommend_shouldUseFallbackWhenNoActivityMatchesVadRanges() {
        List<Activity> activities = List.of(
                activity(1L, ActivityType.RESPIRACION, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.3, -0.3, 0.1),
                activity(2L, ActivityType.DIARIO, -1.0, -0.9, -1.0, -0.9, -1.0, -0.9, 0.35, -0.1, 0.2)
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

        assertThat(result.recommendations().get(0).type()).isEqualTo(ActivityType.DIARIO);
    }

    private EmotionalRecommendationQuery query(
            double valence,
            double arousal,
            double dominance,
            double intensity,
            String userGoal
    ) {
        return new EmotionalRecommendationQuery(
                new Vad(valence, arousal, dominance),
                intensity,
                userGoal
        );
    }

    private List<Activity> defaultActivities() {
        return List.of(
                activity(1L, ActivityType.RESPIRACION, -1.0, 0.3, 0.6, 1.0, -1.0, 1.0, 0.30, -0.30, 0.10),
                activity(2L, ActivityType.DIARIO, -1.0, 0.0, -0.5, 0.5, -0.5, 1.0, 0.35, -0.10, 0.20),
                activity(3L, ActivityType.NUBE, -1.0, 0.2, -0.3, 1.0, -1.0, 1.0, 0.20, -0.20, 0.10),
                activity(4L, ActivityType.BURBUJA, -1.0, 0.3, -1.0, 1.0, -1.0, 0.5, 0.25, 0.05, 0.10)
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
