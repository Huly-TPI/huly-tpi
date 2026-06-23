package com.huly.backend.infrastructure.presentation.dto.comebackReward;

public record ClaimComebackRewardResponse(boolean granted, int coins, int daysInactive) {
}
