package com.huly.backend.domain.model.emotionalRecommendation;

import java.util.List;

public record EmotionalRecommendationResult(
        List<EmotionalRecommendationItem> recommendations,
        boolean fallbackUsed
) {}
