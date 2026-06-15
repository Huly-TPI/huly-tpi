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
        int currentStreak = DailyRewardCycle.isAlive(state, today) ? state.streak() : 0;

        int nextDay = 0;
        int completedDays = 0;
        if (!cycle.isEmpty()) {
            int n = cycle.size();
            if (canClaimToday) {
                nextDay = DailyRewardCycle.cycleDay(DailyRewardCycle.nextStreak(state, today), n);
                completedDays = nextDay - 1;
            } else {
                // Ya reclamó hoy: los días 1..cycleDay(streak) están completos.
                completedDays = DailyRewardCycle.cycleDay(state.streak(), n);
            }
        }

        return new DailyRewardStatus(cycle, currentStreak, completedDays, canClaimToday, nextDay);
    }
}
