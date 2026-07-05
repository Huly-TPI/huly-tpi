package com.huly.backend.infrastructure.presentation.dto.admin.activities;

public record AdminActivityCorrelationResponse(
    String activityType,
    String emotion,
    long suggestionsCount,
    double acceptanceRate
) {}
