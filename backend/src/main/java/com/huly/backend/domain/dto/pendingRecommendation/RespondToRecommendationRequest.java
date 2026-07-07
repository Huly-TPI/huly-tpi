package com.huly.backend.domain.dto.pendingRecommendation;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;

public record RespondToRecommendationRequest(Long recommendationId, Long userId, RecommendationResponseDecision decision) {}
