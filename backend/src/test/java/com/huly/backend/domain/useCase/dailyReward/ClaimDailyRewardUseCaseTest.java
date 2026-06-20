package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DailyRewardAlreadyClaimedException;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardClaim;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimDailyRewardUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private DailyRewardRepository dailyRewardRepository;

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private CoinService coinService;

    private ClaimDailyRewardUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new ClaimDailyRewardUseCase(dailyRewardRepository, userDetailDomainRepository, userPlanRepository, coinService, fixedClock);
    }

    private List<DailyReward> sevenDayCycle() {
        int[] coins = {10, 15, 20, 25, 30, 40, 100};
        return java.util.stream.IntStream.rangeClosed(1, 7)
                .mapToObj(d -> DailyReward.builder().id((long) d).dayNumber(d).coins(coins[d - 1]).build())
                .toList();
    }

    @Test
    void execute_shouldCreditDayOne_whenFirstClaim() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(1);
        assertThat(result.coins()).isEqualTo(10);
        assertThat(result.newStreak()).isEqualTo(1);
        verify(coinService).credit(USER_ID, 10);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 1, TODAY);
    }

    @Test
    void execute_shouldAdvanceStreak_whenClaimIsConsecutive() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(3, TODAY.minusDays(1)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(4);
        assertThat(result.coins()).isEqualTo(25);
        assertThat(result.newStreak()).isEqualTo(4);
        verify(coinService).credit(USER_ID, 25);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 4, TODAY);
    }

    @Test
    void execute_shouldResetToDayOne_whenADayWasMissed() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(3, TODAY.minusDays(2)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(1);
        assertThat(result.coins()).isEqualTo(10);
        verify(coinService).credit(USER_ID, 10);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 1, TODAY);
    }

    @Test
    void execute_shouldWrapCycleButKeepGrowingStreak_whenCycleWasCompleted() {
        // Racha 7 (completó el ciclo) y reclamo consecutivo: el premio vuelve al Día 1,
        // pero la racha total sigue creciendo a 8.
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(7, TODAY.minusDays(1)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(1);
        assertThat(result.coins()).isEqualTo(10);
        assertThat(result.newStreak()).isEqualTo(8);
        verify(coinService).credit(USER_ID, 10);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 8, TODAY);
    }

    @Test
    void execute_shouldContinueSecondCycle_whenStreakAlreadyPastCycle() {
        // Racha 8 (Día 2 del segundo ciclo) consecutiva -> Día 2 / coins 15 / racha 9.
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(8, TODAY.minusDays(1)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(2);
        assertThat(result.coins()).isEqualTo(15);
        assertThat(result.newStreak()).isEqualTo(9);
        verify(coinService).credit(USER_ID, 15);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 9, TODAY);
    }

    @Test
    void execute_shouldThrowAndNotCredit_whenAlreadyClaimedToday() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(2, TODAY));

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(DailyRewardAlreadyClaimedException.class);

        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository, never()).updateDailyClaim(anyLong(), anyInt(), any());
    }

    @Test
    void execute_shouldThrowBusinessRule_whenNoRewardsConfigured() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(BusinessRuleException.class);

        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository, never()).updateDailyClaim(anyLong(), anyInt(), any());
    }

    @Test
    void execute_shouldFallbackToFirstDayCoins_whenCycleDayMissingFromConfig() {
        // Config con "huecos": N=2 filas pero sin el day_number 2 -> el cycleDay calculado (2)
        // no existe en la config y cae al primer día.
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(1, TODAY.minusDays(1)));
        List<DailyReward> gappedCycle = List.of(
                DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                DailyReward.builder().id(2L).dayNumber(5).coins(99).build());
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(gappedCycle);

        DailyRewardClaim result = useCase.execute(USER_ID);

        // newStreak=2 (consecutivo); cycleDay = ((2-1) % 2) + 1 = 2, ausente -> coins del primero (10).
        assertThat(result.newStreak()).isEqualTo(2);
        assertThat(result.dayNumber()).isEqualTo(2);
        assertThat(result.coins()).isEqualTo(10);
        verify(coinService).credit(USER_ID, 10);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 2, TODAY);
    }

    @Test
    void execute_shouldApplyPlanBonus_whenUserHasActivePlan() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());
        UserPlan activePlan = UserPlan.builder()
                .userId(USER_ID)
                .expiresAt(Instant.parse("2026-12-31T00:00:00Z"))
                .build();
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan));

        DailyRewardClaim result = useCase.execute(USER_ID);

        // Día 1 base = 10 -> con plan x1.5 = 15.
        assertThat(result.dayNumber()).isEqualTo(1);
        assertThat(result.coins()).isEqualTo(15);
        verify(coinService).credit(USER_ID, 15);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 1, TODAY);
    }
}
