package com.huly.backend.domain.useCase.admin.userActivities;

import java.time.Instant;

public record ActivitySessionResponse(
        Long id,
        String activityType,
        Instant createdAt
) {
}
