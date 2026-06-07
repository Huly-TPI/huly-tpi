package com.huly.backend.domain.model;

/**
 * Contains the emotional data required to rank wellbeing activities.
 *
 * @param vad current valence, arousal and dominance values
 * @param intensity current emotional intensity from 0 to 1
 * @param userGoal optional goal expressed by the user
 */
public record EmotionalRecommendationQuery(
        Vad vad,
        double intensity,
        String userGoal
) {
}
