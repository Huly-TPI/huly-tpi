package com.huly.backend.infrastructure.presentation.mapper.dailyReward;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.DailyRewardDayResponse;
import com.huly.backend.infrastructure.presentation.dto.dailyReward.DailyRewardStatusResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de recompensa diaria:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class DailyRewardPresentationMapper {

    public GetDailyRewardStatusRequest toStatusRequest(Long userId) {
        return new GetDailyRewardStatusRequest(userId);
    }

    public ClaimDailyRewardRequest toClaimRequest(Long userId) {
        return new ClaimDailyRewardRequest(userId);
    }

    public DailyRewardStatusResponse toStatusResponse(GetDailyRewardStatusResponse status) {
        return new DailyRewardStatusResponse(
                status.days().stream()
                        .map(d -> new DailyRewardDayResponse(
                                d.getDayNumber(),
                                DailyRewardCycle.applyPlanBonus(d.getCoins(), status.planBonusActive())))
                        .toList(),
                status.currentStreak(),
                status.completedDays(),
                status.canClaimToday(),
                status.nextDay(),
                status.planBonusActive()
        );
    }

    public ClaimDailyRewardResponse toClaimResponse(com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse claim) {
        return new ClaimDailyRewardResponse(claim.coins(), claim.dayNumber(), claim.newStreak());
    }
}
