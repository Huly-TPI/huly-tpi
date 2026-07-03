package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardRequest;
import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DailyRewardAlreadyClaimedException;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import com.huly.backend.domain.service.user.UserActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

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
    private PlanService planService;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private CoinService coinService;

    private ClaimDailyRewardUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new ClaimDailyRewardUseCase(dailyRewardRepository, userDetailDomainRepository, planService,
                userActivityService, coinService, fixedClock,
                new com.huly.backend.domain.mapper.dailyReward.ClaimDailyRewardMapper());
    }

    @Test
    @DisplayName("Acredita el Día 1 cuando es el primer reclamo")
    void executeShouldCreditDayOneWhenFirstClaim() {
        givenClaimState(0, null);
        givenConfiguredCycle();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 1, 10, 1);
    }

    @Test
    @DisplayName("Avanza la racha cuando el reclamo es consecutivo")
    void executeShouldAdvanceStreakWhenClaimIsConsecutive() {
        givenClaimState(3, TODAY.minusDays(1));
        givenConfiguredCycle();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 4, 25, 4);
    }

    @Test
    @DisplayName("Reinicia al Día 1 cuando se salteó un día")
    void executeShouldResetToDayOneWhenADayWasMissed() {
        givenClaimState(3, TODAY.minusDays(2));
        givenConfiguredCycle();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 1, 10, 1);
    }

    @Test
    @DisplayName("Reinicia el premio al Día 1 pero la racha total sigue creciendo al completar el ciclo")
    void executeShouldWrapCycleButKeepGrowingStreakWhenCycleWasCompleted() {
        // Racha 7 (completó el ciclo) y reclamo consecutivo: el premio vuelve al Día 1,
        // pero la racha total sigue creciendo a 8.
        givenClaimState(7, TODAY.minusDays(1));
        givenConfiguredCycle();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 1, 10, 8);
    }

    @Test
    @DisplayName("Continúa en el segundo ciclo cuando la racha ya superó el ciclo")
    void executeShouldContinueSecondCycleWhenStreakAlreadyPastCycle() {
        // Racha 8 (Día 2 del segundo ciclo) consecutiva -> Día 2 / coins 15 / racha 9.
        givenClaimState(8, TODAY.minusDays(1));
        givenConfiguredCycle();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 2, 15, 9);
    }

    @Test
    @DisplayName("Lanza excepción y no acredita cuando ya reclamó hoy")
    void executeShouldThrowAndNotCreditWhenAlreadyClaimedToday() {
        givenClaimState(2, TODAY);

        assertThatThrownBy(this::claim).isInstanceOf(DailyRewardAlreadyClaimedException.class);

        thenNothingWasClaimed();
    }

    @Test
    @DisplayName("Lanza error de negocio cuando no hay recompensas configuradas")
    void executeShouldThrowBusinessRuleWhenNoRewardsConfigured() {
        givenClaimState(0, null);
        givenNoConfiguredCycle();

        assertThatThrownBy(this::claim).isInstanceOf(BusinessRuleException.class);

        thenNothingWasClaimed();
    }

    @Test
    @DisplayName("Usa las monedas del primer día cuando el día del ciclo no está en la config")
    void executeShouldFallbackToFirstDayCoinsWhenCycleDayMissingFromConfig() {
        // Config con "huecos": N=2 filas pero sin el day_number 2 -> el cycleDay calculado (2)
        // no existe en la config y cae al primer día (coins del primero = 10).
        givenClaimState(1, TODAY.minusDays(1));
        givenCycle(List.of(
                DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                DailyReward.builder().id(2L).dayNumber(5).coins(99).build()));

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 2, 10, 2);
    }

    @Test
    @DisplayName("Aplica el bonus por plan cuando el usuario tiene un plan activo")
    void executeShouldApplyPlanBonusWhenUserHasActivePlan() {
        // Día 1 base = 10 -> con plan x1.5 = 15.
        givenClaimState(0, null);
        givenConfiguredCycle();
        givenActivePlan();

        ClaimDailyRewardResponse result = claim();

        thenClaimed(result, 1, 15, 1);
    }

    // --- arrange ---

    private void givenClaimState(int streak, LocalDate lastClaimDate) {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(streak, lastClaimDate));
    }

    private void givenConfiguredCycle() {
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());
    }

    private void givenCycle(List<DailyReward> cycle) {
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(cycle);
    }

    private void givenNoConfiguredCycle() {
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(List.of());
    }

    private void givenActivePlan() {
        when(planService.hasActivePlan(eq(USER_ID), any())).thenReturn(true);
    }

    private List<DailyReward> sevenDayCycle() {
        int[] coins = {10, 15, 20, 25, 30, 40, 100};
        return java.util.stream.IntStream.rangeClosed(1, 7)
                .mapToObj(d -> DailyReward.builder().id((long) d).dayNumber(d).coins(coins[d - 1]).build())
                .toList();
    }

    // --- act ---

    private ClaimDailyRewardResponse claim() {
        return useCase.execute(new ClaimDailyRewardRequest(USER_ID));
    }

    // --- assert ---

    /** Verifica la respuesta y los efectos de un reclamo exitoso (monedas acreditadas y racha avanzada). */
    private void thenClaimed(ClaimDailyRewardResponse result, int dayNumber, int coins, int newStreak) {
        assertThat(result.dayNumber()).isEqualTo(dayNumber);
        assertThat(result.coins()).isEqualTo(coins);
        assertThat(result.newStreak()).isEqualTo(newStreak);
        verify(coinService).credit(USER_ID, coins);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, newStreak, TODAY);
    }

    /** Verifica que no se acreditó nada ni se avanzó la racha (reclamo rechazado). */
    private void thenNothingWasClaimed() {
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository, never()).updateDailyClaim(anyLong(), anyInt(), any());
    }
}
