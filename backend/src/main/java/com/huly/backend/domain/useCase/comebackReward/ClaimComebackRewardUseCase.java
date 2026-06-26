package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.domain.mapper.comebackReward.ClaimComebackRewardMapper;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@RequiredArgsConstructor
public class ClaimComebackRewardUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;
    private final CoinService coinService;
    private final Clock clock;
    private final ClaimComebackRewardMapper mapper;

    @Transactional
    public ClaimComebackRewardResponse execute(ClaimComebackRewardRequest request) {
        Long userId = request.userId();
        LocalDate today = LocalDate.now(clock);
        LocalDate lastSeen = userDetailDomainRepository.findLastLoginDate(userId).orElse(null);

        boolean granted = ComebackRewardPolicy.qualifies(lastSeen, today);
        int daysInactive = (int) ComebackRewardPolicy.daysInactive(lastSeen, today);

        int coins = 0;
        if (granted) {
            coins = ComebackRewardPolicy.COMEBACK_COINS;
            coinService.credit(userId, coins);
        }

        // El claim siempre registra la actividad de hoy: consume la recompensa y resetea la brecha.
        userDetailDomainRepository.updateLastLoginDate(userId, today);

        return mapper.toResponse(granted, coins, daysInactive);
    }
}
