package com.huly.backend.presentation.dto.emotionalRecommendation;

import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.enums.ActivityType;

public record EmotionalRecommendationItemResponse(
        Long activityId,
        ActivityType type,
        String title,
        String description,
        double score,
        String reason
) {
    public static EmotionalRecommendationItemResponse from(EmotionalRecommendationItem item) {
        return new EmotionalRecommendationItemResponse(
                item.activityId(),
                item.type(),
                item.title(),
                item.description(),
                item.score(),
                item.reason()
        );
    }
}
