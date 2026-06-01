package com.huly.backend.domain.service;

import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.enums.ActivityType;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class EmotionalRecommendationService {

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
            "calmar", "calmarme", "relajar", "relajarme", "ansiedad", "ansioso", "ansiosa",
            "dormir", "estres", "stress", "tranquilizar", "tranquilizarme"
    );

    private static final Set<String> JOURNAL_GOALS = Set.of(
            "reflexionar", "entender", "escribir", "ordenar", "pensamientos", "diario",
            "procesar", "comprender"
    );

    private static final Set<String> CLOUD_GOALS = Set.of(
            "soltar", "distraerme", "visualizar", "nubes", "nube", "liberar"
    );

    private static final Set<String> BUBBLE_GOALS = Set.of(
            "juego", "jugar", "liviano", "liviana", "distraerme", "burbujas", "burbuja"
    );

    public EmotionalRecommendationResult recommend(EmotionalRecommendationQuery query, List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return new EmotionalRecommendationResult(List.of(), false);
        }

        boolean hasRangeMatch = activities.stream().anyMatch(activity -> isInRange(query, activity));
        List<EmotionalRecommendationItem> recommendations = activities.stream()
                .map(activity -> toRecommendation(query, activity, !hasRangeMatch))
                .sorted(Comparator.comparingDouble(EmotionalRecommendationItem::score).reversed())
                .toList();

        return new EmotionalRecommendationResult(recommendations, !hasRangeMatch);
    }

    private EmotionalRecommendationItem toRecommendation(
            EmotionalRecommendationQuery query,
            Activity activity,
            boolean fallbackUsed
    ) {
        double rangeScore = fallbackUsed ? 0.0 : rangeScore(query, activity);
        double effectScore = effectScore(query, activity);
        double goalScore = goalScore(query.userGoal(), activity.getType());
        double intensityScore = intensityScore(query, activity.getType());

        double score = (rangeScore * 0.45)
                + (effectScore * 0.30)
                + (goalScore * 0.20)
                + (intensityScore * 0.05);

        return new EmotionalRecommendationItem(
                activity.getId(),
                activity.getType(),
                titleFor(activity.getType()),
                descriptionFor(activity.getType()),
                roundScore(score),
                reason(query, activity, fallbackUsed)
        );
    }

    private boolean isInRange(EmotionalRecommendationQuery query, Activity activity) {
        return between(query.valence(), activity.getValenceMin(), activity.getValenceMax())
                && between(query.arousal(), activity.getArousalMin(), activity.getArousalMax())
                && between(query.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
    }

    private double rangeScore(EmotionalRecommendationQuery query, Activity activity) {
        double valenceScore = dimensionRangeScore(query.valence(), activity.getValenceMin(), activity.getValenceMax());
        double arousalScore = dimensionRangeScore(query.arousal(), activity.getArousalMin(), activity.getArousalMax());
        double dominanceScore = dimensionRangeScore(query.dominance(), activity.getDominanceMin(), activity.getDominanceMax());
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

    private double effectScore(EmotionalRecommendationQuery query, Activity activity) {
        double score = 0.0;
        int factors = 0;

        if (query.arousal() > 0.5) {
            score += positiveNegativeEffect(activity.getEffectArousal());
            factors++;
        }
        if (query.valence() < -0.2) {
            score += positiveEffect(activity.getEffectValence());
            factors++;
        }
        if (query.dominance() < -0.2) {
            score += positiveEffect(activity.getEffectDominance());
            factors++;
        }

        if (factors == 0) {
            return generalEffectScore(activity);
        }
        return score / factors;
    }

    private double positiveNegativeEffect(double effect) {
        return effect < 0 ? Math.min(1.0, Math.abs(effect) * 3.0) : 0.0;
    }

    private double positiveEffect(double effect) {
        return effect > 0 ? Math.min(1.0, effect * 3.0) : 0.0;
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

    private double intensityScore(EmotionalRecommendationQuery query, ActivityType type) {
        if (query.intensity() < 0.65) {
            return 0.0;
        }
        if (type == ActivityType.RESPIRACION && query.arousal() > 0.5) {
            return 1.0;
        }
        if (type == ActivityType.NUBE && query.valence() < -0.3) {
            return 0.7;
        }
        if (type == ActivityType.DIARIO && query.arousal() <= 0.6) {
            return 0.6;
        }
        return 0.2;
    }

    private String reason(EmotionalRecommendationQuery query, Activity activity, boolean fallbackUsed) {
        if (fallbackUsed) {
            return "Fallback por falta de coincidencia exacta en rangos VAD; se ordeno por efectos esperados y objetivo.";
        }
        if (activity.getType() == ActivityType.RESPIRACION && query.arousal() > 0.5) {
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
}
