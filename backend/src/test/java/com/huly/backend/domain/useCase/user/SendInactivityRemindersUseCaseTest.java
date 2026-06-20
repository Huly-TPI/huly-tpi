package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.model.user.InactiveUserToRemind;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendInactivityRemindersUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-19T10:00:00Z");
    private static final int THRESHOLD_DAYS = 5;
    private static final int REWARD_COINS = 30;

    @Mock private UserDetailDomainRepository userDetailDomainRepository;
    @Mock private EmailPort emailPort;

    private SendInactivityRemindersUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        useCase = new SendInactivityRemindersUseCase(
                userDetailDomainRepository, emailPort, fixedClock, THRESHOLD_DAYS, REWARD_COINS);
    }

    @Test
    void execute_shouldEmailAndMark_eachInactiveUser_withThresholdNowMinusDays() {
        Instant expectedThreshold = NOW.minus(Duration.ofDays(THRESHOLD_DAYS));
        when(userDetailDomainRepository.findUsersNeedingInactivityReminder(expectedThreshold))
                .thenReturn(List.of(
                        new InactiveUserToRemind(1L, "ana@huly.com", "Ana"),
                        new InactiveUserToRemind(2L, "leo@huly.com", "Leo")));

        useCase.execute();

        verify(emailPort).sendInactivityReminder("ana@huly.com", "Ana", REWARD_COINS);
        verify(emailPort).sendInactivityReminder("leo@huly.com", "Leo", REWARD_COINS);
        verify(userDetailDomainRepository).markInactivityReminderSent(1L, NOW);
        verify(userDetailDomainRepository).markInactivityReminderSent(2L, NOW);
    }

    @Test
    void execute_shouldDoNothing_whenNoInactiveUsers() {
        when(userDetailDomainRepository.findUsersNeedingInactivityReminder(any()))
                .thenReturn(List.of());

        useCase.execute();

        verify(emailPort, never()).sendInactivityReminder(anyString(), anyString(), anyInt());
        verify(userDetailDomainRepository, never()).markInactivityReminderSent(anyLong(), any());
        assertThat(true).isTrue();
    }
}
