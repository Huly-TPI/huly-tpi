package com.huly.backend.infrastructure.presentation.dto.userGoal;

import java.util.List;

public record UserGoalPageResponse(
        List<UserGoalResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}
