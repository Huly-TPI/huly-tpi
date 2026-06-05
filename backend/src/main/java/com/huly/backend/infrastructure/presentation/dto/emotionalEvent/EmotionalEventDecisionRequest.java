package com.huly.backend.infrastructure.presentation.dto.emotionalEvent;

import com.huly.backend.domain.model.enums.RecommendationDecision;
import jakarta.validation.constraints.NotNull;

public record EmotionalEventDecisionRequest(
        @NotNull
        RecommendationDecision decision,

        Long chosenActivityId
) {}
