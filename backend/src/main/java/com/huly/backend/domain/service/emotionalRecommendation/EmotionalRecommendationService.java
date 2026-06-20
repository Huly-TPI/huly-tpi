package com.huly.backend.domain.service.emotionalRecommendation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Calculates deterministic activity rankings from VAD state and recommendation context.
 */
@Service
public class EmotionalRecommendationService {

    private static final double RANGE_WEIGHT = 0.45;
    private static final double EFFECT_WEIGHT = 0.30;
    private static final double GOAL_WEIGHT = 0.20;
    private static final double INTENSITY_WEIGHT = 0.05;
    private static final double EFFECT_SCALE = 3.0;
    private static final double HIGH_AROUSAL_THRESHOLD = 0.5;
    private static final double LOW_VALENCE_THRESHOLD = -0.2;
    private static final double LOW_DOMINANCE_THRESHOLD = -0.2;
    private static final double HIGH_INTENSITY_THRESHOLD = 0.65;
    private static final double CLOUD_LOW_VALENCE_THRESHOLD = -0.3;
    private static final double JOURNAL_MAX_AROUSAL = 0.6;
    private static final double MAX_TREND_ADJUSTMENT = 0.15;
    private static final double RECENT_EVENT_DECAY = 0.05;
    private static final double MIN_RECENCY_WEIGHT = 0.40;
    private static final double ACCEPTED_SIGNAL = 0.70;
    private static final double CHOSE_OTHER_CHOSEN_SIGNAL = 0.80;
    private static final double CHOSE_OTHER_RECOMMENDED_SIGNAL = -0.40;
    private static final double IGNORED_SIGNAL = -0.30;
    private static final Map<Integer, Double> FEEDBACK_SIGNALS = Map.of(
            1, -1.00,
            2, -0.50,
            3, 0.00,
            4, 0.50,
            5, 1.00
    );

    private static final Map<ActivityType, String> TITLES = Map.of(
            ActivityType.RESPIRACION, "Respiracion guiada",
            ActivityType.DIARIO, "Diario emocional",
            ActivityType.NUBE, "Nubes emocionales",
            ActivityType.BURBUJA, "Burbujas"
    );

    private static final Map<ActivityType, String> DESCRIPTIONS = Map.of(
            ActivityType.RESPIRACION, "Una practica breve para bajar la activacion y recuperar calma.",
            ActivityType.DIARIO, "Un espacio para ordenar pensamientos y entender lo que sentis.",
            ActivityType.NUBE, "Un ejercicio visual para soltar pensamientos que pesan.",
            ActivityType.BURBUJA, "Una actividad liviana para cambiar el foco con suavidad."
    );

    private static final Set<String> BREATHING_GOALS = Set.of(
            "calmar", "calmarme", "calmarse", "relajar", "relajarme", "relajarse",
            "ansiedad", "ansioso", "ansiosa", "dormir", "estres", "stress",
            "respirar", "tranquilizar", "tranquilizarme", "tranquilizarse"
    );

    private static final Set<String> JOURNAL_GOALS = Set.of(
            "reflexionar", "entender", "escribir", "ordenar", "pensamientos", "diario",
            "procesar", "comprender", "claridad", "aclarar"
    );

    private static final Set<String> CLOUD_GOALS = Set.of(
            "soltar", "visualizar", "nubes", "nube", "liberar", "rumiar", "rumia"
    );

    private static final Set<String> BUBBLE_GOALS = Set.of(
            "juego", "jugar", "liviano", "liviana", "distraer", "distraerme", "distraerse",
            "burbujas", "burbuja", "desconectar", "despejarme"
    );

    /**
     * Ranks activities according to VAD compatibility, expected effects, user goal and intensity.
     *
     * @param query emotional state used for ranking
     * @param activities available activities
     * @return ordered recommendations and whether the VAD range fallback was used
     */
    public EmotionalRecommendationResult recommend(EmotionalRecommendation query, List<Activity> activities) {
        return recommend(query, activities, List.of());
    }

    /**
     * Ranks activities and moderately adapts the score using the user's recommendation history.
     *
     * @param query emotional state used for ranking
     * @param activities available activities
     * @param userHistory recent emotional events with decisions or feedback
     * @return ordered recommendations and whether the VAD range fallback was used
     */
    public EmotionalRecommendationResult recommend(
            EmotionalRecommendation query,
            List<Activity> activities,
            List<EmotionalEvent> userHistory
    ) {
        if (activities == null || activities.isEmpty()) {
            return new EmotionalRecommendationResult(List.of(), false);
        }

        Vad vad = query.vad();
        boolean hasRangeMatch = activities.stream().anyMatch(activity -> isInRange(vad, activity));
        Map<ActivityType, TrendAccumulator> trends = buildTrends(userHistory, activities);
        List<EmotionalRecommendationItem> recommendations = activities.stream()
                .map(activity -> toRecommendation(query, activity, !hasRangeMatch, trends))
                .sorted(Comparator.comparingDouble(EmotionalRecommendationItem::score).reversed())
                .toList();

        return new EmotionalRecommendationResult(recommendations, !hasRangeMatch);
    }

    private EmotionalRecommendationItem toRecommendation(
            EmotionalRecommendation query,
            Activity activity,
            boolean fallbackUsed,
            Map<ActivityType, TrendAccumulator> trends
    ) {
        double rangeScore = fallbackUsed ? 0.0 : rangeScore(query.vad(), activity);
        double effectScore = effectScore(query.vad(), activity);
        double goalScore = goalScore(query.userGoal(), activity.getType());
        double intensityScore = intensityScore(query, activity.getType());

        double score = (rangeScore * RANGE_WEIGHT)
                + (effectScore * EFFECT_WEIGHT)
                + (goalScore * GOAL_WEIGHT)
                + (intensityScore * INTENSITY_WEIGHT);
        double adjustedScore = score + trendAdjustment(activity.getType(), trends);

        return new EmotionalRecommendationItem(
                activity.getId(),
                activity.getType(),
                titleFor(activity.getType()),
                descriptionFor(activity.getType()),
                roundScore(adjustedScore),
                reason(query, activity, fallbackUsed)
        );
    }

    private boolean isInRange(Vad vad, Activity activity) {
        return between(vad.valence(), activity.getValenceMin(), activity.getValenceMax())
                && between(vad.arousal(), activity.getArousalMin(), activity.getArousalMax())
                && between(vad.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
    }

    private double rangeScore(Vad vad, Activity activity) {
        double valenceScore = dimensionRangeScore(vad.valence(), activity.getValenceMin(), activity.getValenceMax());
        double arousalScore = dimensionRangeScore(vad.arousal(), activity.getArousalMin(), activity.getArousalMax());
        double dominanceScore = dimensionRangeScore(vad.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
        return (valenceScore + arousalScore + dominanceScore) / 3.0;
    }

    private double dimensionRangeScore(double value, double min, double max) {
        if (between(value, min, max)) {
            return 1.0;
        }
        double distance = value < min ? min - value : value - max;
        return Math.max(0.0, 1.0 - distance);
    }

    private boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private double effectScore(Vad vad, Activity activity) {
        double score = 0.0;
        int factors = 0;

        if (vad.arousal() > HIGH_AROUSAL_THRESHOLD) {
            score += positiveNegativeEffect(activity.getEffectArousal());
            factors++;
        }
        if (vad.valence() < LOW_VALENCE_THRESHOLD) {
            score += positiveEffect(activity.getEffectValence());
            factors++;
        }
        if (vad.dominance() < LOW_DOMINANCE_THRESHOLD) {
            score += positiveEffect(activity.getEffectDominance());
            factors++;
        }

        if (factors == 0) {
            return generalEffectScore(activity);
        }
        return score / factors;
    }

    private double positiveNegativeEffect(double effect) {
        return effect < 0 ? Math.min(1.0, Math.abs(effect) * EFFECT_SCALE) : 0.0;
    }

    private double positiveEffect(double effect) {
        return effect > 0 ? Math.min(1.0, effect * EFFECT_SCALE) : 0.0;
    }

    private double generalEffectScore(Activity activity) {
        double valence = positiveEffect(activity.getEffectValence());
        double arousal = activity.getEffectArousal() < 0
                ? positiveNegativeEffect(activity.getEffectArousal())
                : Math.min(1.0, activity.getEffectArousal());
        double dominance = positiveEffect(activity.getEffectDominance());
        return (valence + arousal + dominance) / 3.0;
    }

    private double goalScore(String userGoal, ActivityType type) {
        if (type == null) {
            return 0.0;
        }
        String normalizedGoal = normalize(userGoal);
        if (normalizedGoal.isBlank()) {
            return 0.0;
        }
        return switch (type) {
            case RESPIRACION -> containsAny(normalizedGoal, BREATHING_GOALS) ? 1.0 : 0.0;
            case DIARIO -> containsAny(normalizedGoal, JOURNAL_GOALS) ? 1.0 : 0.0;
            case NUBE -> containsAny(normalizedGoal, CLOUD_GOALS) ? 1.0 : 0.0;
            case BURBUJA -> containsAny(normalizedGoal, BUBBLE_GOALS) ? 1.0 : 0.0;
            default -> 0.0;
        };
    }

    private boolean containsAny(String value, Set<String> keywords) {
        return keywords.stream().anyMatch(value::contains);
    }

    private double intensityScore(EmotionalRecommendation query, ActivityType type) {
        if (query.intensity() < HIGH_INTENSITY_THRESHOLD) {
            return 0.0;
        }
        if (type == ActivityType.RESPIRACION && query.vad().arousal() > HIGH_AROUSAL_THRESHOLD) {
            return 1.0;
        }
        if (type == ActivityType.NUBE && query.vad().valence() < CLOUD_LOW_VALENCE_THRESHOLD) {
            return 0.7;
        }
        if (type == ActivityType.DIARIO && query.vad().arousal() <= JOURNAL_MAX_AROUSAL) {
            return 0.6;
        }
        return 0.2;
    }

    private String reason(EmotionalRecommendation query, Activity activity, boolean fallbackUsed) {
        if (fallbackUsed) {
            return "Fallback por falta de coincidencia exacta en rangos VAD; se ordeno por efectos esperados y objetivo.";
        }
        if (activity.getType() == ActivityType.RESPIRACION
                && query.vad().arousal() > HIGH_AROUSAL_THRESHOLD) {
            return "Recomendada porque el arousal es alto y la actividad ayuda a reducir activacion.";
        }
        if (activity.getType() == ActivityType.DIARIO) {
            return "Recomendada para ordenar pensamientos y procesar la emocion detectada.";
        }
        if (activity.getType() == ActivityType.NUBE) {
            return "Recomendada para soltar pensamientos y bajar carga emocional.";
        }
        return "Recomendada por compatibilidad con el estado emocional y sus efectos esperados.";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String titleFor(ActivityType type) {
        if (type == null) {
            return "Actividad";
        }
        return TITLES.getOrDefault(type, humanize(type.name()));
    }

    private String descriptionFor(ActivityType type) {
        return DESCRIPTIONS.getOrDefault(type, "Actividad sugerida por compatibilidad con el estado emocional actual.");
    }

    private String humanize(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private double roundScore(double score) {
        return Math.round(Math.min(1.0, Math.max(0.0, score)) * 100.0) / 100.0;
    }

    private Map<ActivityType, TrendAccumulator> buildTrends(
            List<EmotionalEvent> userHistory,
            List<Activity> activities
    ) {
        if (userHistory == null || userHistory.isEmpty()) {
            return Map.of();
        }

        Map<Long, ActivityType> activityTypesById = activityTypesById(activities);
        Map<ActivityType, TrendAccumulator> trends = new HashMap<>();
        for (int index = 0; index < userHistory.size(); index++) {
            EmotionalEvent event = userHistory.get(index);
            double recencyWeight = recencyWeight(index);
            applyDecisionSignal(event, activityTypesById, trends, recencyWeight);
            applyFeedbackSignal(event, activityTypesById, trends, recencyWeight);
        }
        return trends;
    }

    private Map<Long, ActivityType> activityTypesById(List<Activity> activities) {
        Map<Long, ActivityType> result = new HashMap<>();
        for (Activity activity : activities) {
            if (activity.getId() != null && activity.getType() != null) {
                result.put(activity.getId(), activity.getType());
            }
        }
        return result;
    }

    private double recencyWeight(int index) {
        return Math.max(MIN_RECENCY_WEIGHT, 1.0 - (index * RECENT_EVENT_DECAY));
    }

    private void applyDecisionSignal(
            EmotionalEvent event,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, TrendAccumulator> trends,
            double recencyWeight
    ) {
        if (event == null || event.getRecommendationDecision() == null) {
            return;
        }

        RecommendationDecision decision = event.getRecommendationDecision();
        if (decision == RecommendationDecision.ACCEPTED) {
            addSignal(preferredActivityType(event, activityTypesById), trends, ACCEPTED_SIGNAL, recencyWeight);
            return;
        }
        if (decision == RecommendationDecision.IGNORED) {
            addSignal(activityTypesById.get(event.getRecommendedActivityId()), trends, IGNORED_SIGNAL, recencyWeight);
            return;
        }
        if (decision == RecommendationDecision.CHOSE_OTHER) {
            addSignal(
                    activityTypesById.get(event.getRecommendedActivityId()),
                    trends,
                    CHOSE_OTHER_RECOMMENDED_SIGNAL,
                    recencyWeight
            );
            addSignal(
                    activityTypesById.get(event.getChosenActivityId()),
                    trends,
                    CHOSE_OTHER_CHOSEN_SIGNAL,
                    recencyWeight
            );
        }
    }

    private void applyFeedbackSignal(
            EmotionalEvent event,
            Map<Long, ActivityType> activityTypesById,
            Map<ActivityType, TrendAccumulator> trends,
            double recencyWeight
    ) {
        if (event == null || event.getFeedbackScore() == null) {
            return;
        }

        Double signal = FEEDBACK_SIGNALS.get(event.getFeedbackScore());
        if (signal != null && signal != 0.0) {
            addSignal(preferredActivityType(event, activityTypesById), trends, signal, recencyWeight);
        }
    }

    private ActivityType preferredActivityType(EmotionalEvent event, Map<Long, ActivityType> activityTypesById) {
        ActivityType chosenType = activityTypesById.get(event.getChosenActivityId());
        if (chosenType != null) {
            return chosenType;
        }
        return activityTypesById.get(event.getRecommendedActivityId());
    }

    private void addSignal(
            ActivityType type,
            Map<ActivityType, TrendAccumulator> trends,
            double signal,
            double recencyWeight
    ) {
        if (type == null) {
            return;
        }
        trends.computeIfAbsent(type, key -> new TrendAccumulator()).add(signal, recencyWeight);
    }

    private double trendAdjustment(ActivityType type, Map<ActivityType, TrendAccumulator> trends) {
        if (type == null || trends == null || trends.isEmpty()) {
            return 0.0;
        }
        TrendAccumulator trend = trends.get(type);
        if (trend == null) {
            return 0.0;
        }
        return clamp(trend.score() * MAX_TREND_ADJUSTMENT, -MAX_TREND_ADJUSTMENT, MAX_TREND_ADJUSTMENT);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class TrendAccumulator {
        private double signalSum;
        private double weightSum;

        void add(double signal, double recencyWeight) {
            signalSum += signal * recencyWeight;
            weightSum += recencyWeight;
        }

        double score() {
            return weightSum == 0.0 ? 0.0 : signalSum / weightSum;
        }
    }
}
