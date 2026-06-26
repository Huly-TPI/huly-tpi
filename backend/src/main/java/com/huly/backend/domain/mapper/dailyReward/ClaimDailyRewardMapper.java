package com.huly.backend.domain.mapper.dailyReward;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse;

/**
 * Mapper de dominio para el caso de uso de reclamo de recompensa diaria.
 */
public class ClaimDailyRewardMapper {

    public ClaimDailyRewardResponse toResponse(int coins, int dayNumber, int newStreak) {
        return new ClaimDailyRewardResponse(coins, dayNumber, newStreak);
    }
}
