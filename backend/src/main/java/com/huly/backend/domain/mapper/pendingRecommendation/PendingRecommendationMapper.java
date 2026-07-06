package com.huly.backend.domain.mapper.pendingRecommendation;

import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;

public class PendingRecommendationMapper {

    public PendingRecommendationResponse toResponse(PendingDailyRecommendation recommendation, boolean isNew) {
        return new PendingRecommendationResponse(
                recommendation.getId(),
                recommendation.getRecommendationDate(),
                recommendation.getDecision(),
                recommendation.getRecommendedTaskIds(),
                isNew,
                true
        );
    }

    public PendingRecommendationResponse notApplicable() {
        return new PendingRecommendationResponse(null, null, null, java.util.List.of(), false, false);
    }
}
