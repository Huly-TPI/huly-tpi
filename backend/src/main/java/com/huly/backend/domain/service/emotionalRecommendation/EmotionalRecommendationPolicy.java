package com.huly.backend.domain.service.emotionalRecommendation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.Vad;
import com.huly.backend.domain.model.enums.ActivityType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmotionalRecommendationPolicy {

    public static final double RANGE_WEIGHT = 0.45;
    public static final double EFFECT_WEIGHT = 0.30;
    public static final double GOAL_WEIGHT = 0.20;
    public static final double INTENSITY_WEIGHT = 0.05;
    
    public static final double MAX_TREND_ADJUSTMENT = 0.10;
    public static final double RECENT_EVENT_DECAY = 0.05;
    public static final double MIN_RECENCY_WEIGHT = 0.40;
    
    public static final double ACCEPTED_SIGNAL = 0.70;
    public static final double CHOSE_OTHER_CHOSEN_SIGNAL = 0.80;
    public static final double CHOSE_OTHER_RECOMMENDED_SIGNAL = -0.40;
    public static final double IGNORED_SIGNAL = -0.30;
    
    private static final double EFFECT_SCALE = 3.0;
    private static final double HIGH_AROUSAL_THRESHOLD = 0.5;
    private static final double LOW_VALENCE_THRESHOLD = -0.2;
    private static final double LOW_DOMINANCE_THRESHOLD = -0.2;
    private static final double HIGH_INTENSITY_THRESHOLD = 0.65;
    private static final double CLOUD_LOW_VALENCE_THRESHOLD = -0.3;
    private static final double JOURNAL_MAX_AROUSAL = 0.6;
    
    public static final Map<Integer, Double> FEEDBACK_SIGNALS = Map.of(
            1, -1.00,
            2, -0.50,
            3, 0.00,
            4, 0.50,
            5, 1.00
    );

    public boolean isInRange(Vad vad, Activity activity) {
        return between(vad.valence(), activity.getValenceMin(), activity.getValenceMax())
                && between(vad.arousal(), activity.getArousalMin(), activity.getArousalMax())
                && between(vad.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
    }

    public double calculateBaseScore(EmotionalRecommendation query, Activity activity, boolean fallbackUsed) {
        double rangeScore = fallbackUsed ? 0.0 : rangeScore(query.vad(), activity);
        double effectScore = effectScore(query.vad(), activity);
        double goalScore = goalScore(query.userGoal(), activity);
        double intensityScore = intensityScore(query, activity.getType());

        return (rangeScore * RANGE_WEIGHT)
                + (effectScore * EFFECT_WEIGHT)
                + (goalScore * GOAL_WEIGHT)
                + (intensityScore * INTENSITY_WEIGHT);
    }

    public double rangeScore(Vad vad, Activity activity) {
        double valenceDist = distance(vad.valence(), activity.getValenceMin(), activity.getValenceMax());
        double arousalDist = distance(vad.arousal(), activity.getArousalMin(), activity.getArousalMax());
        double dominanceDist = distance(vad.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
        return 1.0 - ((valenceDist + arousalDist + dominanceDist) / 3.0);
    }

    public double effectScore(Vad vad, Activity activity) {
        if (activity == null) {
            return 0.0;
        }
        if (vad.valence() < LOW_VALENCE_THRESHOLD) {
            return positiveEffect(activity.getEffectValence());
        }
        if (vad.arousal() > HIGH_AROUSAL_THRESHOLD) {
            return positiveNegativeEffect(activity.getEffectArousal());
        }
        if (vad.dominance() < LOW_DOMINANCE_THRESHOLD) {
            return positiveEffect(activity.getEffectDominance());
        }
        return generalEffectScore(activity);
    }

    public double goalScore(String userGoal, Activity activity) {
        if (activity == null || activity.getGoalKeywords() == null || activity.getGoalKeywords().isBlank()) {
            return 0.0;
        }
        String normalizedGoal = normalize(userGoal);
        if (normalizedGoal.isBlank()) {
            return 0.0;
        }
        Set<String> keywords = Arrays.stream(activity.getGoalKeywords().split(","))
                .map(this::normalize)
                .filter(k -> !k.isBlank())
                .collect(Collectors.toSet());
        return containsAny(normalizedGoal, keywords) ? 1.0 : 0.0;
    }

    public double intensityScore(EmotionalRecommendation query, ActivityType type) {
        if (query.intensity() < HIGH_INTENSITY_THRESHOLD) {
            return 0.0;
        }
        return switch (type) {
            case BREATHING -> query.vad().arousal() > HIGH_AROUSAL_THRESHOLD ? 1.0 : 0.2;
            case LANTERN -> query.vad().valence() < CLOUD_LOW_VALENCE_THRESHOLD ? 0.7 : 0.2;
            case DIARY -> query.vad().arousal() <= JOURNAL_MAX_AROUSAL ? 0.6 : 0.2;
            case ZEN_GARDEN -> query.vad().arousal() > HIGH_AROUSAL_THRESHOLD ? 0.9 : 0.2;
            case MANDALA -> query.vad().arousal() > HIGH_AROUSAL_THRESHOLD ? 0.8 : 0.2;
            default -> 0.2;
        };
    }

    public String reason(EmotionalRecommendation query, Activity activity, boolean fallbackUsed) {
        if (fallbackUsed) {
            return "Fallback por falta de coincidencia exacta en rangos VAD; se ordeno por efectos esperados y objetivo.";
        }
        return switch (activity.getType()) {
            case BREATHING -> query.vad().arousal() > HIGH_AROUSAL_THRESHOLD
                    ? "Recomendada porque el arousal es alto y la actividad ayuda a reducir activacion."
                    : "Recomendada por compatibilidad con el estado emocional y sus efectos esperados.";
            case DIARY -> "Recomendada para ordenar pensamientos y procesar la emocion detectada.";
            case LANTERN -> "Recomendada para soltar pensamientos y bajar carga emocional.";
            case ZEN_GARDEN -> "Recomendada para calmar la mente a traves de dibujos en la arena.";
            case MANDALA -> "Recomendada para centrar la atencion coloreando formas complejas.";
            case CHALLENGE -> "Recomendada para activarte mediante un reto adaptado a tu estado actual.";
            default -> "Recomendada por compatibilidad con el estado emocional y sus efectos esperados.";
        };
    }

    public String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private double distance(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0;
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

    private boolean containsAny(String value, Set<String> keywords) {
        return keywords.stream().anyMatch(value::contains);
    }
}
