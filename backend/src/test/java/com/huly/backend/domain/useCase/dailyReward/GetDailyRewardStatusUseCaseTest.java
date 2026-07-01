package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.PlanService;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDailyRewardStatusUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private DailyRewardRepository dailyRewardRepository;

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @Mock
    private PlanService planService;

    private GetDailyRewardStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new GetDailyRewardStatusUseCase(dailyRewardRepository, userDetailDomainRepository, planService, fixedClock,
                new com.huly.backend.domain.mapper.dailyReward.GetDailyRewardStatusMapper());
    }

    @Test
    @DisplayName("Devuelve todo en cero cuando no hay recompensas configuradas")
    void executeShouldReturnZerosWhenNoRewardsConfigured() {
        givenClaimState(0, null);
        givenNoConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        assertThat(status.days()).isEmpty();
        thenStatusWas(status, true, 0, 0, 0);
    }

    @Test
    @DisplayName("Reporta el próximo día y la racha viva cuando es consecutivo y puede reclamar")
    void executeShouldReportNextDayAndAliveStreakWhenConsecutiveAndCanClaim() {
        givenClaimState(3, TODAY.minusDays(1));
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        thenStatusWas(status, true, 3, 4, 3);
    }

    @Test
    @DisplayName("Reinicia la racha cuando se salteó un día")
    void executeShouldResetStreakWhenADayWasMissed() {
        givenClaimState(3, TODAY.minusDays(2));
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        // racha rota -> no viva (currentStreak 0)
        thenStatusWas(status, true, 0, 1, 0);
    }

    @Test
    @DisplayName("Arranca en el Día 1 cuando es el primer reclamo")
    void executeShouldStartAtDayOneWhenFirstClaim() {
        givenClaimState(0, null);
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        thenStatusWas(status, true, 0, 1, 0);
    }

    @Test
    @DisplayName("Marca los días completos cuando ya reclamó hoy")
    void executeShouldMarkCompletedDaysWhenAlreadyClaimedToday() {
        givenClaimState(3, TODAY);
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        thenStatusWas(status, false, 3, 0, 3);
    }

    @Test
    @DisplayName("Usa la posición del ciclo cuando ya reclamó hoy tras reiniciar el ciclo")
    void executeShouldUseCyclePositionWhenAlreadyClaimedTodayAfterWrap() {
        givenClaimState(8, TODAY);
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        // cycleDay(8, 7) = 1
        thenStatusWas(status, false, 8, 0, 1);
    }

    @Test
    @DisplayName("No reporta bonus cuando el usuario no tiene plan")
    void executeShouldReportNoBonusWhenUserHasNoPlan() {
        givenClaimState(0, null);
        givenConfiguredCycle();

        GetDailyRewardStatusResponse status = status();

        thenPlanBonus(status, false);
    }

    @Test
    @DisplayName("Reporta bonus cuando el usuario tiene un plan activo")
    void executeShouldReportBonusWhenUserHasActivePlan() {
        givenClaimState(0, null);
        givenConfiguredCycle();
        givenActivePlan();

        GetDailyRewardStatusResponse status = status();

        thenPlanBonus(status, true);
    }

    // --- arrange ---

    private void givenClaimState(int streak, LocalDate lastClaimDate) {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(streak, lastClaimDate));
    }

    private void givenConfiguredCycle() {
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());
    }

    private void givenNoConfiguredCycle() {
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(List.of());
    }

    private void givenActivePlan() {
        when(planService.hasActivePlan(eq(USER_ID), any())).thenReturn(true);
    }

    private List<DailyReward> sevenDayCycle() {
        int[] coins = {10, 15, 20, 25, 30, 40, 100};
        return IntStream.rangeClosed(1, 7)
                .mapToObj(d -> DailyReward.builder().id((long) d).dayNumber(d).coins(coins[d - 1]).build())
                .toList();
    }

    // --- act ---

    private GetDailyRewardStatusResponse status() {
        return useCase.execute(new GetDailyRewardStatusRequest(USER_ID));
    }

    // --- assert ---

    /** Verifica los campos de progreso del estado (si puede reclamar, racha vigente, próximo día y días completos). */
    private void thenStatusWas(GetDailyRewardStatusResponse status, boolean canClaimToday,
                               int currentStreak, int nextDay, int completedDays) {
        assertThat(status.canClaimToday()).isEqualTo(canClaimToday);
        assertThat(status.currentStreak()).isEqualTo(currentStreak);
        assertThat(status.nextDay()).isEqualTo(nextDay);
        assertThat(status.completedDays()).isEqualTo(completedDays);
    }

    private void thenPlanBonus(GetDailyRewardStatusResponse status, boolean expected) {
        assertThat(status.planBonusActive()).isEqualTo(expected);
    }
}
