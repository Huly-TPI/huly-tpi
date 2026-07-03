package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.mapper.dailyReward.GetDailyRewardStatusMapper;
import com.huly.backend.domain.model.dailyReward.CycleProgress;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.PlanService;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class GetDailyRewardStatusUseCase {

    private final DailyRewardRepository dailyRewardRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final PlanService planService;
    private final Clock clock;
    private final GetDailyRewardStatusMapper mapper;

    public GetDailyRewardStatusResponse execute(GetDailyRewardStatusRequest request) {
        Long userId = request.userId();
        LocalDate today = LocalDate.now(clock);

        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(userId);
        List<DailyReward> cycle = dailyRewardRepository.findAllOrderByDay();

        boolean canClaimToday = !state.claimedOn(today);
        int currentStreak = DailyRewardCycle.currentStreak(state, today);
        CycleProgress progress = DailyRewardCycle.progress(state, today, cycle.size(), canClaimToday);
        boolean planBonusActive = planService.hasActivePlan(userId, Instant.now(clock));

        return mapper.toResponse(cycle, currentStreak, progress.completedDays(),
                canClaimToday, progress.nextDay(), planBonusActive);
    }
}
