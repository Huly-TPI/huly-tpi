package com.huly.backend.infrastructure.presentation.dto.userGoal;

public record UserGoalListResponse(
        UserGoalPageResponse completados,
        UserGoalPageResponse pendientes
) {}
