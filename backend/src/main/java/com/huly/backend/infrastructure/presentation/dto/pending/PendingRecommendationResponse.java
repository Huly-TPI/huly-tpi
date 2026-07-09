package com.huly.backend.infrastructure.presentation.dto.pending;

import java.time.LocalDate;
import java.util.List;

public record PendingRecommendationResponse(
        Long recommendationId,
        LocalDate date,
        String decision,
        List<Long> recommendedTaskIds,
        boolean isNew
) {}
