package com.huly.backend.domain.useCase.dailyReward;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DailyRewardAlreadyClaimedException;
import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.model.dailyReward.DailyRewardClaim;
import com.huly.backend.domain.repository.DailyRewardRepository;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
import org.junit.jupiter.api.BeforeEach;
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
    private CoinService coinService;

    private ClaimDailyRewardUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new ClaimDailyRewardUseCase(dailyRewardRepository, userDetailDomainRepository, coinService, fixedClock);
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
    void execute_shouldWrapToDayOne_whenCycleWasCompleted() {
        when(userDetailDomainRepository.findDailyClaimState(USER_ID))
                .thenReturn(new DailyClaimState(7, TODAY.minusDays(1)));
        when(dailyRewardRepository.findAllOrderByDay()).thenReturn(sevenDayCycle());

        DailyRewardClaim result = useCase.execute(USER_ID);

        assertThat(result.dayNumber()).isEqualTo(1);
        assertThat(result.coins()).isEqualTo(10);
        verify(coinService).credit(USER_ID, 10);
        verify(userDetailDomainRepository).updateDailyClaim(USER_ID, 1, TODAY);
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
}
