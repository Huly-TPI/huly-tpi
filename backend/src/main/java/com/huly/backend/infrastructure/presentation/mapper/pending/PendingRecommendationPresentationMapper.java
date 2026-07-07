package com.huly.backend.infrastructure.presentation.mapper.pending;

import com.huly.backend.infrastructure.presentation.dto.pending.PendingRecommendationResponse;
import org.springframework.stereotype.Component;

@Component
public class PendingRecommendationPresentationMapper {

    public PendingRecommendationResponse toResponse(com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse response) {
        return new PendingRecommendationResponse(
                response.recommendationId(),
                response.date(),
                response.decision() == null ? null : response.decision().name(),
                response.recommendedTaskIds(),
                response.isNew()
        );
    }
}
