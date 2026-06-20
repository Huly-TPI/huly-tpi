package com.huly.backend.domain.model.emotionalRecommendation;

import com.huly.backend.domain.model.enums.RecommendationDecision;

public record UpdateRecommendationDecisionCommand(
        RecommendationDecision decision,
        Long chosenActivityId
) {}
