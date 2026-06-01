package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.enums.EmotionType;

public record EmotionalAnalysisResult(
        boolean shouldRecommend,
        EmotionType detectedEmotion,
        double confidence,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String userGoal,
        String shortReason
) {

    public static EmotionalAnalysisResult neutral() {
        return new EmotionalAnalysisResult(false, EmotionType.NEUTRAL, 0.0, 0.0, 0.0, 0.0, 0.0, null, null);
    }
}
