package com.huly.backend.presentation.dto.emotionalRecommendation;

import com.huly.backend.domain.model.EmotionalRecommendationResult;

import java.util.List;

public record EmotionalRecommendationResponse(
        List<EmotionalRecommendationItemResponse> recommendations,
        boolean fallbackUsed
) {
    public static EmotionalRecommendationResponse from(EmotionalRecommendationResult result) {
        return new EmotionalRecommendationResponse(
                result.recommendations().stream()
                        .map(EmotionalRecommendationItemResponse::from)
                        .toList(),
                result.fallbackUsed()
        );
    }
}
