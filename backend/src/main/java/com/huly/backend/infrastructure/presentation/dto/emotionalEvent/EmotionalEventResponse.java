package com.huly.backend.infrastructure.presentation.dto.emotionalEvent;

import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;

import java.time.Instant;

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
    public static EmotionalEventResponse from(EmotionalEvent event) {
        return new EmotionalEventResponse(
                event.getId(),
                event.getUserId(),
                event.getSource(),
                event.getInputText(),
                event.getDetectedEmotion(),
                event.getConfidence(),
                event.getValence(),
                event.getArousal(),
                event.getDominance(),
                event.getIntensity(),
                event.getUserGoal(),
                event.getGeneratedRecommendation(),
                event.getRecommendedActivityId(),
                event.getChosenActivityId(),
                event.getRecommendationDecision(),
                event.getFeedbackScore(),
                event.getFeedbackText(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
