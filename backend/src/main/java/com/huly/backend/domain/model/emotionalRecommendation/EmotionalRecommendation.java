package com.huly.backend.domain.model.emotionalRecommendation;

/**
 * Contains the emotional data required to rank wellbeing activities.
 *
 * @param userId optional user identifier used to adapt ranking from recommendation history
 * @param vad current valence, arousal and dominance values
 * @param intensity current emotional intensity from 0 to 1
 * @param userGoal optional goal expressed by the user
 */
public record EmotionalRecommendation(
        Long userId,
        Vad vad,
        double intensity,
        String userGoal
) {
    public EmotionalRecommendation(Vad vad, double intensity, String userGoal) {
        this(null, vad, intensity, userGoal);
    }
}
