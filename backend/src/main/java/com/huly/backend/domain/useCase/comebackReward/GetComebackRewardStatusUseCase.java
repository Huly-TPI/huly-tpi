package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

@RequiredArgsConstructor
public class GetComebackRewardStatusUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;
    private final Clock clock;

    public GetComebackRewardStatusResponse execute(GetComebackRewardStatusRequest request) {
        Long userId = request.userId();
        LocalDate today = LocalDate.now(clock);
        LocalDate lastSeen = userDetailDomainRepository.findLastLoginDate(userId).orElse(null);

        boolean available = ComebackRewardPolicy.qualifies(lastSeen, today);
        int daysInactive = (int) ComebackRewardPolicy.daysInactive(lastSeen, today);

        return new GetComebackRewardStatusResponse(
                available,
                daysInactive,
                ComebackRewardPolicy.COMEBACK_COINS,
                ComebackRewardPolicy.INACTIVE_DAYS_THRESHOLD
        );
    }
}
