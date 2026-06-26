package com.huly.backend.infrastructure.presentation.mapper.comebackReward;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.infrastructure.presentation.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.infrastructure.presentation.dto.comebackReward.ComebackRewardStatusResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de recompensa por regreso:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class ComebackRewardPresentationMapper {

    public GetComebackRewardStatusRequest toStatusRequest(Long userId) {
        return new GetComebackRewardStatusRequest(userId);
    }

    public ClaimComebackRewardRequest toClaimRequest(Long userId) {
        return new ClaimComebackRewardRequest(userId);
    }

    public ComebackRewardStatusResponse toStatusResponse(GetComebackRewardStatusResponse status) {
        return new ComebackRewardStatusResponse(
                status.available(), status.daysInactive(), status.coins(), status.thresholdDays());
    }

    public ClaimComebackRewardResponse toClaimResponse(com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse result) {
        return new ClaimComebackRewardResponse(result.granted(), result.coins(), result.daysInactive());
    }
}
