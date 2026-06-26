package com.huly.backend.domain.dto.emotionalRecommendation;

import java.util.List;

/**
 * Respuesta de dominio con las recomendaciones emocionales rankeadas.
 */
public record GetEmotionalRecommendationsResponse(
        List<EmotionalRecommendationItem> recommendations,
        boolean fallbackUsed
) {
}
