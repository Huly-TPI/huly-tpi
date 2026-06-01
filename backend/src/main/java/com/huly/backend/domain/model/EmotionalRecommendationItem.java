package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.ActivityType;

public record EmotionalRecommendationItem(
        Long activityId,
        ActivityType type,
        String title,
        String description,
        double score,
        String reason
) {}
