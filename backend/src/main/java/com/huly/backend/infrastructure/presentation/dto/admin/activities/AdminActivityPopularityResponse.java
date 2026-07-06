package com.huly.backend.infrastructure.presentation.dto.admin.activities;

public record AdminActivityPopularityResponse(
    String activityType,
    String activityName,
    long totalSessions
) {}
