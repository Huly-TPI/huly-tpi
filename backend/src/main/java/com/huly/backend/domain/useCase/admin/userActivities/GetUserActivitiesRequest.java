package com.huly.backend.domain.useCase.admin.userActivities;

import com.huly.backend.domain.model.enums.Timeframe;

public record GetUserActivitiesRequest(Long userId, Timeframe timeframe) {
}

