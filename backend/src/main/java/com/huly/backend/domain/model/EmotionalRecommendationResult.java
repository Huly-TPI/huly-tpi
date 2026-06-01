package com.huly.backend.domain.model;

import java.util.List;

public record EmotionalRecommendationResult(
        List<EmotionalRecommendationItem> recommendations,
        boolean fallbackUsed
) {}
