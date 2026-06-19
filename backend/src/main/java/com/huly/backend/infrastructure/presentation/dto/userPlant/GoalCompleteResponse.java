package com.huly.backend.infrastructure.presentation.dto.userPlant;

import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;

public record GoalCompleteResponse(
        UserGoalResponse goal,
        boolean harvestTriggered,
        Integer harvestedPlantNumber,
        UserPlantSummaryResponse currentPlant
) {}
