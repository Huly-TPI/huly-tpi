package com.huly.backend.presentation.dto.userGoal;

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
