package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.rewards.DailyRewardRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
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
    private UserPlanRepository userPlanRepository;

    private GetDailyRewardStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new GetDailyRewardStatusUseCase(dailyRewardRepository, userDetailDomainRepository, userPlanRepository, fixedClock);
    }

    private List<DailyReward> sevenDayCycle() {
        int[] coins = {10, 15, 20, 25, 30, 40, 100};
        return IntStream.rangeClosed(1, 7)
                .mapToObj(d -> DailyReward.builder().id((long) d).dayNumber(d).coins(coins[d - 1]).build())
                .toList();
    }

    @Test
    void execute_shouldReturnZeros_whenNoRewardsConfigured() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID)).thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(List.of());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.days()).isEmpty();
        assertThat(status.currentStreak()).isEqualTo(0);
        assertThat(status.completedDays()).isEqualTo(0);
        assertThat(status.canClaimToday()).isTrue();
        assertThat(status.nextDay()).isEqualTo(0);
    }

    @Test
    void execute_shouldReportNextDayAndAliveStreak_whenConsecutiveAndCanClaim() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(3, TODAY.minusDays(1)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.canClaimToday()).isTrue();
        assertThat(status.currentStreak()).isEqualTo(3);
        assertThat(status.nextDay()).isEqualTo(4);
        assertThat(status.completedDays()).isEqualTo(3);
    }

    @Test
    void execute_shouldResetStreak_whenADayWasMissed() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(3, TODAY.minusDays(2)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.canClaimToday()).isTrue();
        assertThat(status.currentStreak()).isEqualTo(0); // racha rota -> no viva
        assertThat(status.nextDay()).isEqualTo(1);
        assertThat(status.completedDays()).isEqualTo(0);
    }

    @Test
    void execute_shouldStartAtDayOne_whenFirstClaim() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID)).thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.currentStreak()).isEqualTo(0);
        assertThat(status.nextDay()).isEqualTo(1);
        assertThat(status.completedDays()).isEqualTo(0);
    }

    @Test
    void execute_shouldMarkCompletedDays_whenAlreadyClaimedToday() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(3, TODAY));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.canClaimToday()).isFalse();
        assertThat(status.currentStreak()).isEqualTo(3);
        assertThat(status.completedDays()).isEqualTo(3);
        assertThat(status.nextDay()).isEqualTo(0);
    }

    @Test
    void execute_shouldUseCyclePosition_whenAlreadyClaimedTodayAfterWrap() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(8, TODAY));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.canClaimToday()).isFalse();
        assertThat(status.currentStreak()).isEqualTo(8);
        assertThat(status.completedDays()).isEqualTo(1); // cycleDay(8, 7) = 1
    }

    @Test
    void execute_shouldReportNoBonus_whenUserHasNoPlan() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID)).thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.planBonusActive()).isFalse();
    }

    @Test
    void execute_shouldReportBonus_whenUserHasActivePlan() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID)).thenReturn(new DailyClaimState(0, null));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());
        UserPlan activePlan = UserPlan.builder()
                .userId(USER_ID)
                .expiresAt(Instant.parse("2026-12-31T00:00:00Z"))
                .build();
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan));

        GetDailyRewardStatusResponse status = useCase.execute(new GetDailyRewardStatusRequest(USER_ID));

        assertThat(status.planBonusActive()).isTrue();
    }
}
