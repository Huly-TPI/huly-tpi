package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardRequest;
import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.domain.mapper.dailyReward.ClaimDailyRewardMapper;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DailyRewardAlreadyClaimedException;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardCycle;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import com.huly.backend.domain.service.user.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ClaimDailyRewardUseCase {

    private final DailyRewardRepository dailyRewardRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;
    private final PlanService planService;
    private final UserActivityService userActivityService;
    private final CoinService coinService;
    private final Clock clock;
    private final ClaimDailyRewardMapper mapper;

    @Transactional
    public ClaimDailyRewardResponse execute(ClaimDailyRewardRequest request) {
        Long userId = request.userId();
        LocalDate today = LocalDate.now(clock);

        DailyClaimState state = resolveClaimState(userId, today);
        List<DailyReward> cycle = loadConfiguredCycle();
        ClaimedReward reward = resolveReward(state, today, cycle, userId);
        applyClaim(userId, today, reward);

        return mapper.toResponse(reward.coins(), reward.cycleDay(), reward.newStreak());
    }

    /** Obtiene el estado de racha del usuario y valida que aún no haya reclamado hoy. */
    private DailyClaimState resolveClaimState(Long userId, LocalDate today) {
        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(userId);
        if (state.claimedOn(today)) {
            throw new DailyRewardAlreadyClaimedException("Ya reclamaste tu recompensa de hoy.");
        }
        return state;
    }

    private List<DailyReward> loadConfiguredCycle() {
        List<DailyReward> cycle = dailyRewardRepository.findAllOrderByDay();
        if (cycle.isEmpty()) {
            throw new BusinessRuleException("No hay recompensas diarias configuradas.");
        }
        return cycle;
    }

    /** Calcula la racha, el día del ciclo y las monedas (con bonus por plan) que corresponden hoy. */
    private ClaimedReward resolveReward(DailyClaimState state, LocalDate today, List<DailyReward> cycle, Long userId) {
        int newStreak = DailyRewardCycle.nextStreak(state, today);
        int cycleDay = DailyRewardCycle.cycleDay(newStreak, cycle.size());
        boolean hasPlan = planService.hasActivePlan(userId, Instant.now(clock));
        int coins = DailyRewardCycle.applyPlanBonus(DailyRewardCycle.coinsForDay(cycle, cycleDay), hasPlan);
        return new ClaimedReward(coins, cycleDay, newStreak);
    }

    /** Persiste el reclamo: acredita las monedas, avanza la racha y registra la actividad. */
    private void applyClaim(Long userId, LocalDate today, ClaimedReward reward) {
        coinService.credit(userId, reward.coins());
        userDetailDomainRepository.updateDailyClaim(userId, reward.newStreak(), today);
        userActivityService.registerActivity(userId, today);
    }

    private record ClaimedReward(int coins, int cycleDay, int newStreak) {
    }
}
