package com.huly.backend.domain.mapper.dailyReward;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyReward;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de estado de recompensa diaria.
 */
public class GetDailyRewardStatusMapper {

    public GetDailyRewardStatusResponse toResponse(List<DailyReward> days,
                                                   int currentStreak,
                                                   int completedDays,
                                                   boolean canClaimToday,
                                                   int nextDay,
                                                   boolean planBonusActive) {
        return new GetDailyRewardStatusResponse(days, currentStreak, completedDays, canClaimToday, nextDay, planBonusActive);
    }
}
