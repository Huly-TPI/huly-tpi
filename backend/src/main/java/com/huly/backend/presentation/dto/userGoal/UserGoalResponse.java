package com.huly.backend.presentation.dto.userGoal;

import java.time.Instant;

public record UserGoalResponse(
        Long id,
        Long userId,
        String title,
        String description,
        String status,
        Instant createdAt,
        Long activityId
) {}
