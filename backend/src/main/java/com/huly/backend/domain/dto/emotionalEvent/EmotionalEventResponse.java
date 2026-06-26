package com.huly.backend.domain.dto.emotionalEvent;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;

import java.time.Instant;

/**
 * Respuesta de dominio que representa el estado de un evento emocional.
 */
public record EmotionalEventResponse(
        Long id,
        Long userId,
        EmotionalEventSource source,
        String inputText,
        String detectedEmotion,
        Double confidence,
        Double valence,
        Double arousal,
        Double dominance,
        Double intensity,
        String userGoal,
        String generatedRecommendation,
        Long recommendedActivityId,
        Long chosenActivityId,
        RecommendationDecision recommendationDecision,
        Integer feedbackScore,
        String feedbackText,
        Instant createdAt,
        Instant updatedAt
) {
}
