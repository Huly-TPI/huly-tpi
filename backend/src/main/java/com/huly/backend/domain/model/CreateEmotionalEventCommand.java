package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.EmotionalEventSource;

public record CreateEmotionalEventCommand(
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
) {}
