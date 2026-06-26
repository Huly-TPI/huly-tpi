package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimComebackRewardUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Mock
    private UserDetailDomainRepository userDetailDomainRepository;

    @Mock
    private CoinService coinService;

    private ClaimComebackRewardUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.from(ZoneOffset.UTC));
        useCase = new ClaimComebackRewardUseCase(userDetailDomainRepository, coinService, fixedClock,
                new com.huly.backend.domain.mapper.comebackReward.ClaimComebackRewardMapper());
    }

    @Test
    void execute_shouldGrantAndCredit_whenThresholdReached() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(10)));

        ClaimComebackRewardResponse result = useCase.execute(new ClaimComebackRewardRequest(USER_ID));

        assertThat(result.granted()).isTrue();
        assertThat(result.coins()).isEqualTo(ComebackRewardPolicy.COMEBACK_COINS);
        assertThat(result.daysInactive()).isEqualTo(10);
        verify(coinService).credit(USER_ID, ComebackRewardPolicy.COMEBACK_COINS);
        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }

    @Test
    void execute_shouldNotGrant_whenBelowThreshold() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(TODAY.minusDays(3)));

        ClaimComebackRewardResponse result = useCase.execute(new ClaimComebackRewardRequest(USER_ID));

        assertThat(result.granted()).isFalse();
        assertThat(result.coins()).isEqualTo(0);
        assertThat(result.daysInactive()).isEqualTo(3);
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }

    @Test
    void execute_shouldNotGrantButRegister_whenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());

        ClaimComebackRewardResponse result = useCase.execute(new ClaimComebackRewardRequest(USER_ID));

        assertThat(result.granted()).isFalse();
        assertThat(result.coins()).isEqualTo(0);
        assertThat(result.daysInactive()).isEqualTo(0);
        verify(coinService, never()).credit(anyLong(), anyInt());
        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }
}
