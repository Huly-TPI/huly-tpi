package com.huly.backend.domain.mapper.comebackReward;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;

/**
 * Mapper de dominio para el caso de uso de reclamo de recompensa por regreso.
 */
public class ClaimComebackRewardMapper {

    public ClaimComebackRewardResponse toResponse(boolean granted, int coins, int daysInactive) {
        return new ClaimComebackRewardResponse(granted, coins, daysInactive);
    }
}
