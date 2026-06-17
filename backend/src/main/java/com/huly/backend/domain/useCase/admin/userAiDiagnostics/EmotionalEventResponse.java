package com.huly.backend.domain.useCase.admin.userAiDiagnostics;

import java.time.Instant;

public record EmotionalEventResponse(
        Long id,
        String source,
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
        String recommendationDecision,
        Integer feedbackScore,
        String feedbackText,
        Instant createdAt
) {
}
