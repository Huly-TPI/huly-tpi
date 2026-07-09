package com.huly.backend.domain.dto.admin.activities;

public record UpdateActivityConfigRequest(
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
