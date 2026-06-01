package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.EmotionalEventSource;

public record EmotionalRecommendationQuery(
        Long userId,
        EmotionalEventSource source,
        String inputText,
        String detectedEmotion,
        Double confidence,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String userGoal
) {}
