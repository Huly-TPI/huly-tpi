package com.huly.backend.infrastructure.presentation.dto.userPlant;

import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;

import java.util.List;

public record PlantGoalsResponse(
        Long plantId,
        List<UserGoalResponse> goals
) {}
