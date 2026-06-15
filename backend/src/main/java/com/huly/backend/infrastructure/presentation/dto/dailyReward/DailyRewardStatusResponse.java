package com.huly.backend.infrastructure.presentation.dto.dailyReward;

import java.util.List;

public record DailyRewardStatusResponse(
        List<DailyRewardDayResponse> days,
        int currentStreak,
        int completedDays,
        boolean canClaimToday,
        int nextDay
) {
}
