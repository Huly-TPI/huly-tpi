package com.huly.backend.domain.model.chat;

import com.huly.backend.domain.model.Vad;
import com.huly.backend.domain.model.enums.EmotionType;

/**
 * Structured emotional analysis produced for a conversation message.
 */
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

    /**
     * Returns the VAD dimensions as a single value object.
     *
     * @return current VAD state
     */
    public Vad vad() {
        return new Vad(valence, arousal, dominance);
    }
}
