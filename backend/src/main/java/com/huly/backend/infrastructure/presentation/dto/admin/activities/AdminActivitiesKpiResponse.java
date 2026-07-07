package com.huly.backend.infrastructure.presentation.dto.admin.activities;

public record AdminActivitiesKpiResponse(
    long totalSessions,
    TopActivity topActivity,
    double averageMoodImprovement
) {
    public record TopActivity(
        String type,
        long sessions
    ) {}
}
