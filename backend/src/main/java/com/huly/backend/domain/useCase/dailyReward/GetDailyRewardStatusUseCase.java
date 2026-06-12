package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.domain.model.dailyReward.DailyRewardStatus;
import com.huly.backend.domain.repository.DailyRewardRepository;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class GetDailyRewardStatusUseCase {

    private final DailyRewardRepository dailyRewardRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final Clock clock;

    public DailyRewardStatus execute(Long userId) {
        LocalDate today = LocalDate.now(clock);
        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(userId);
        List<DailyReward> cycle = dailyRewardRepository.findAllOrderByDay();

        boolean canClaimToday = !today.equals(state.lastClaimDate());
        int nextDay = cycle.isEmpty()
                ? 0
                : DailyRewardCycle.computeNextDay(state, today, cycle.size());

        return new DailyRewardStatus(cycle, state.streak(), canClaimToday, nextDay);
    }
}
