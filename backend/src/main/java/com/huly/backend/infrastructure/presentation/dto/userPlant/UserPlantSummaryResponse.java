package com.huly.backend.infrastructure.presentation.dto.userPlant;

import java.time.Instant;

public record UserPlantSummaryResponse(
        Long id,
        Integer plantNumber,
        Integer requiredGoals,
        Long completedGoalsCount,
        String status,
        Instant startedAt,
        Instant completedAt
) {}
