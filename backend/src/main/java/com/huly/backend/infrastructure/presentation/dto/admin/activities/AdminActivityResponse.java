package com.huly.backend.infrastructure.presentation.dto.admin.activities;

import com.huly.backend.domain.model.enums.ActivityType;

public record AdminActivityResponse(
        Long id,
        ActivityType type,
        double valenceMin,
        double valenceMax,
        double arousalMin,
        double arousalMax,
        double dominanceMin,
        double dominanceMax,
        double effectValence,
        double effectArousal,
        double effectDominance,
        String title,
        String description,
        String goalKeywords,
        String routePath
) {}
