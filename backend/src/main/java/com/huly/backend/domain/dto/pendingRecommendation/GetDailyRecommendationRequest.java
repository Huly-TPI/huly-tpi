package com.huly.backend.domain.dto.pendingRecommendation;

import java.time.LocalDate;

public record GetDailyRecommendationRequest(Long userId, LocalDate today, boolean forceRedecide) {

    public GetDailyRecommendationRequest(Long userId, LocalDate today) {
        this(userId, today, false);
    }
}
