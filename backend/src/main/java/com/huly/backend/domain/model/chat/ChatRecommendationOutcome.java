package com.huly.backend.domain.model.chat;

public record ChatRecommendationOutcome(
        EmotionalAnalysisResult analysis,
        SuggestedChatAction suggestedAction
) {

    public static ChatRecommendationOutcome none(EmotionalAnalysisResult analysis) {
        return new ChatRecommendationOutcome(analysis, null);
    }
}
