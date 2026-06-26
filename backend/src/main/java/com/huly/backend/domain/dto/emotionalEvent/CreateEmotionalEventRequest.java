package com.huly.backend.domain.dto.emotionalEvent;

import com.huly.backend.domain.model.enums.EmotionalEventSource;

/**
 * Pedido de dominio para crear un evento emocional.
 */
public record CreateEmotionalEventRequest(
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
        Long chosenActivityId
) {
}
