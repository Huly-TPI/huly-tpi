package com.huly.backend.domain.mapper.comebackReward;

import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;

/**
 * Mapper de dominio para el caso de uso de estado de recompensa por regreso.
 */
public class GetComebackRewardStatusMapper {

    public GetComebackRewardStatusResponse toResponse(boolean available, int daysInactive, int coins, int thresholdDays) {
        return new GetComebackRewardStatusResponse(available, daysInactive, coins, thresholdDays);
    }
}
