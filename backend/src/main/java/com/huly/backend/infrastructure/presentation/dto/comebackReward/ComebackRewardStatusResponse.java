package com.huly.backend.infrastructure.presentation.dto.comebackReward;

public record ComebackRewardStatusResponse(
        boolean available,
        int daysInactive,
        int coins,
        int thresholdDays
) {
}
