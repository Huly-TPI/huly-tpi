package com.huly.backend.domain.useCase.comebackReward;

import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.model.comebackReward.ComebackRewardPolicy;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
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
        useCase = new GetComebackRewardStatusUseCase(userDetailDomainRepository, fixedClock,
                new com.huly.backend.domain.mapper.comebackReward.GetComebackRewardStatusMapper());
    }

    @Test
    @DisplayName("No está disponible cuando el usuario nunca fue visto")
    void executeShouldNotBeAvailableWhenNeverSeen() {
        // --- arrange ---
        givenNeverSeen();

        // --- act ---
        GetComebackRewardStatusResponse status = getStatus();

        // --- assert ---
        thenNotAvailableAfterInactivity(status, 0);
        thenAdvertisesRewardAmount(status);
        thenAdvertisesThreshold(status);
    }

    @Test
    @DisplayName("No está disponible cuando el usuario fue visto hoy")
    void executeShouldNotBeAvailableWhenSeenToday() {
        // --- arrange ---
        givenLastSeen(TODAY);

        // --- act ---
        GetComebackRewardStatusResponse status = getStatus();

        // --- assert ---
        thenNotAvailableAfterInactivity(status, 0);
    }

    @Test
    @DisplayName("No está disponible cuando la inactividad no alcanza el umbral")
    void executeShouldNotBeAvailableWhenBelowThreshold() {
        // --- arrange ---
        givenLastSeen(TODAY.minusDays(9));

        // --- act ---
        GetComebackRewardStatusResponse status = getStatus();

        // --- assert ---
        thenNotAvailableAfterInactivity(status, 9);
    }

    @Test
    @DisplayName("Está disponible cuando la inactividad alcanza el umbral")
    void executeShouldBeAvailableWhenThresholdReached() {
        // --- arrange ---
        givenLastSeen(TODAY.minusDays(10));

        // --- act ---
        GetComebackRewardStatusResponse status = getStatus();

        // --- assert ---
        thenAvailableAfterInactivity(status, 10);
        thenAdvertisesRewardAmount(status);
    }

    @Test
    @DisplayName("No está disponible y recorta los días inactivos a cero cuando la última actividad es futura")
    void executeShouldClampInactiveDaysToZeroWhenLastSeenIsInTheFuture() {
        // Última actividad posterior a hoy (p. ej. desfase de reloj): la brecha negativa se recorta a 0.
        // --- arrange ---
        givenLastSeen(TODAY.plusDays(5));

        // --- act ---
        GetComebackRewardStatusResponse status = getStatus();

        // --- assert ---
        thenNotAvailableAfterInactivity(status, 0);
    }

    // --- arrange ---

    private void givenNeverSeen() {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenLastSeen(LocalDate lastSeen) {
        when(userDetailDomainRepository.findLastLoginDate(USER_ID)).thenReturn(Optional.of(lastSeen));
    }

    // --- act ---

    private GetComebackRewardStatusResponse getStatus() {
        return useCase.execute(new GetComebackRewardStatusRequest(USER_ID));
    }

    // --- assert ---

    private void thenNotAvailableAfterInactivity(GetComebackRewardStatusResponse status, int daysInactive) {
        assertThat(status.available()).isFalse();
        assertThat(status.daysInactive()).isEqualTo(daysInactive);
    }

    private void thenAvailableAfterInactivity(GetComebackRewardStatusResponse status, int daysInactive) {
        assertThat(status.available()).isTrue();
        assertThat(status.daysInactive()).isEqualTo(daysInactive);
    }

    private void thenAdvertisesRewardAmount(GetComebackRewardStatusResponse status) {
        assertThat(status.coins()).isEqualTo(ComebackRewardPolicy.COMEBACK_COINS);
    }

    private void thenAdvertisesThreshold(GetComebackRewardStatusResponse status) {
        assertThat(status.thresholdDays()).isEqualTo(ComebackRewardPolicy.INACTIVE_DAYS_THRESHOLD);
    }
}
