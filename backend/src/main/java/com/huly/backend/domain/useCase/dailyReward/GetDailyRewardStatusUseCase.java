package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.mapper.dailyReward.GetDailyRewardStatusMapper;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class GetDailyRewardStatusUseCase {

    private final DailyRewardRepository dailyRewardRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final UserPlanRepository userPlanRepository;
    private final Clock clock;
    private final GetDailyRewardStatusMapper mapper;

    public GetDailyRewardStatusResponse execute(GetDailyRewardStatusRequest request) {
        Long userId = request.userId();
        LocalDate today = LocalDate.now(clock);
        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(userId);
        List<DailyReward> cycle = dailyRewardRepository.findAllOrderByDay();

        boolean planBonusActive = userPlanRepository.findByUser(userId)
                .filter(p -> p.isActive(Instant.now(clock)))
                .isPresent();

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

        return mapper.toResponse(cycle, currentStreak, completedDays, canClaimToday, nextDay, planBonusActive);
    }
}
