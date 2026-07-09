package com.huly.backend.infrastructure.presentation.dto.admin.activities;

public record AdminActivityImpactResponse(
    String activityType,
    double averageValenceChange,
    double averageArousalChange,
    boolean basedOnMetrics
) {}
