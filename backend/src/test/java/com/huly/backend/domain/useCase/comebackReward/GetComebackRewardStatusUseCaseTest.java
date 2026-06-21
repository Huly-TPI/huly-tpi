package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetComebackRewardStatusUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    private GetComebackRewardStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new GetComebackRewardStatusUseCase(userDetailDomainRepository, fixedClock);
    }

    @Test
    void execute_shouldNotBeAvailable_whenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());

        GetComebackRewardStatusResponse status = useCase.execute(new GetComebackRewardStatusRequest(USER_ID));

        assertThat(status.available()).isFalse();
        assertThat(status.daysInactive()).isEqualTo(0);
        assertThat(status.coins()).isEqualTo(ComebackRewardPolicy.COMEBACK_COINS);
        assertThat(status.thresholdDays()).isEqualTo(ComebackRewardPolicy.INACTIVE_DAYS_THRESHOLD);
    }

    @Test
    void execute_shouldNotBeAvailable_whenSeenToday() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY));

        GetComebackRewardStatusResponse status = useCase.execute(new GetComebackRewardStatusRequest(USER_ID));

        assertThat(status.available()).isFalse();
        assertThat(status.daysInactive()).isEqualTo(0);
    }

    @Test
    void execute_shouldNotBeAvailable_whenBelowThreshold() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(9)));

        GetComebackRewardStatusResponse status = useCase.execute(new GetComebackRewardStatusRequest(USER_ID));

        assertThat(status.available()).isFalse();
        assertThat(status.daysInactive()).isEqualTo(9);
    }

    @Test
    void execute_shouldBeAvailable_whenThresholdReached() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(10)));

        GetComebackRewardStatusResponse status = useCase.execute(new GetComebackRewardStatusRequest(USER_ID));

        assertThat(status.available()).isTrue();
        assertThat(status.daysInactive()).isEqualTo(10);
        assertThat(status.coins()).isEqualTo(ComebackRewardPolicy.COMEBACK_COINS);
    }
}
