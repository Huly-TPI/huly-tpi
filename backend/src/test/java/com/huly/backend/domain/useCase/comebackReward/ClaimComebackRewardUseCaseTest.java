package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.service.payment.CoinService;
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
    @DisplayName("Otorga y acredita las monedas cuando se alcanza el umbral de inactividad")
    void executeShouldGrantAndCreditWhenThresholdReached() {
        // --- arrange ---
        givenLastSeen(TODAY.minusDays(10));

        // --- act ---
        ClaimComebackRewardResponse result = claim();

        // --- assert ---
        thenGranted(result, 10);
        thenCredited();
        thenRegisteredActivity();
    }

    @Test
    @DisplayName("No otorga pero registra la actividad cuando no se alcanza el umbral")
    void executeShouldNotGrantWhenBelowThreshold() {
        // --- arrange ---
        givenLastSeen(TODAY.minusDays(3));

        // --- act ---
        ClaimComebackRewardResponse result = claim();

        // --- assert ---
        thenNotGranted(result, 3);
        thenNotCredited();
        thenRegisteredActivity();
    }

    @Test
    @DisplayName("No otorga pero registra la actividad cuando el usuario nunca fue visto")
    void executeShouldNotGrantButRegisterWhenNeverSeen() {
        // --- arrange ---
        givenNeverSeen();

        // --- act ---
        ClaimComebackRewardResponse result = claim();

        // --- assert ---
        thenNotGranted(result, 0);
        thenNotCredited();
        thenRegisteredActivity();
    }

    // --- arrange ---

    private void givenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenLastSeen(LocalDate lastSeen) {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(lastSeen));
    }

    // --- act ---

    private ClaimComebackRewardResponse claim() {
        return useCase.execute(new ClaimComebackRewardRequest(USER_ID));
    }

    // --- assert ---

    private void thenGranted(ClaimComebackRewardResponse result, int daysInactive) {
        assertThat(result.granted()).isTrue();
        assertThat(result.coins()).isEqualTo(ComebackRewardPolicy.COMEBACK_COINS);
        assertThat(result.daysInactive()).isEqualTo(daysInactive);
    }

    private void thenNotGranted(ClaimComebackRewardResponse result, int daysInactive) {
        assertThat(result.granted()).isFalse();
        assertThat(result.coins()).isEqualTo(0);
        assertThat(result.daysInactive()).isEqualTo(daysInactive);
    }

    private void thenCredited() {
        verify(coinService).credit(USER_ID, ComebackRewardPolicy.COMEBACK_COINS);
    }

    private void thenNotCredited() {
        verify(coinService, never()).credit(anyLong(), anyInt());
    }

    private void thenRegisteredActivity() {
        verify(userDetailDomainRepository).updateLastLoginDate(USER_ID, TODAY);
    }
}
