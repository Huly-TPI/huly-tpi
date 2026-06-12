package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DailyRewardAlreadyClaimedException;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardClaim;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.domain.repository.DailyRewardRepository;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ClaimDailyRewardUseCase {

    private final DailyRewardRepository dailyRewardRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final CoinService coinService;
    private final Clock clock;

    @Transactional
    public DailyRewardClaim execute(Long userId) {
        LocalDate today = LocalDate.now(clock);
        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(userId);

        if (today.equals(state.lastClaimDate())) {
            throw new DailyRewardAlreadyClaimedException("Ya reclamaste tu recompensa de hoy.");
        }

        List<DailyReward> cycle = dailyRewardRepository.findAllOrderByDay();
        if (cycle.isEmpty()) {
            throw new BusinessRuleException("No hay recompensas diarias configuradas.");
        }

        int nextDay = DailyRewardCycle.computeNextDay(state, today, cycle.size());
        int coins = resolveCoins(cycle, nextDay);

        coinService.credit(userId, coins);
        userDetailDomainRepository.updateDailyClaim(userId, nextDay, today);

        return new DailyRewardClaim(coins, nextDay, nextDay);
    }

    private int resolveCoins(List<DailyReward> cycle, int dayNumber) {
        return cycle.stream()
                .filter(r -> r.getDayNumber() == dayNumber)
                .findFirst()
                .map(DailyReward::getCoins)
                // Si la config no tuviera ese día (N variable), cae al primer día.
                .orElseGet(() -> cycle.get(0).getCoins());
    }
}
