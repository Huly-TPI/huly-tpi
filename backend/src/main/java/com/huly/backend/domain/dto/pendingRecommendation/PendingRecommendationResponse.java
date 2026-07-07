package com.huly.backend.domain.dto.pendingRecommendation;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;

import java.time.LocalDate;
import java.util.List;

public record PendingRecommendationResponse(
        Long recommendationId,
        LocalDate date,
        RecommendationResponseDecision decision,
        List<Long> recommendedTaskIds,
        boolean isNew,
        boolean applicable
) {}
