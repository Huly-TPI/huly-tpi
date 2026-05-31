package com.huly.backend.presentation.dto.userGoal;

public record UserGoalListResponse(
        UserGoalPageResponse completados,
        UserGoalPageResponse pendientes
) {}
