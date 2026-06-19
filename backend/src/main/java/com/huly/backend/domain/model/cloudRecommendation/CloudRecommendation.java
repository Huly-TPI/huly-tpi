package com.huly.backend.domain.model.cloudRecommendation;

public record CloudRecommendation(
        String activityType,
        String actionId,
        String title,
        String description,
        String redirectUrl
) {}
