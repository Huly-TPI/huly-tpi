package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanExpiryReminderSchedulerTest {

    @Mock
    private UserPlanRepository userPlanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private PlanExpiryReminderScheduler scheduler;

    private AppUser user(Long id, String email) {
        return AppUser.builder()
                .id(id).email(email)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void sendExpiryReminders_shouldSendAndMark_whenPlanIsExpiringSoon() {
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.DAYS);
        UserPlan plan = UserPlan.builder()
                .id(10L).userId(1L).planCode("PREMIUM").expiresAt(expiresAt)
                .build();
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(plan));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "user@huly")));

        scheduler.sendExpiryReminders();

        verify(emailPort).sendPlanExpiryReminder(eq("user@huly"), anyLong(), eq(expiresAt));
        verify(userPlanRepository).markExpiryReminderSent(10L, expiresAt);
    }

    @Test
    void sendExpiryReminders_shouldDoNothing_whenNoPlansNeedReminder() {
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());

        scheduler.sendExpiryReminders();

        verify(emailPort, never()).sendPlanExpiryReminder(any(), anyLong(), any());
        verify(userPlanRepository, never()).markExpiryReminderSent(anyLong(), any());
    }

    @Test
    void sendExpiryReminders_shouldNotSendNorMark_whenUserMissing() {
        UserPlan plan = UserPlan.builder()
                .id(10L).userId(99L).planCode("PREMIUM")
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(plan));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        scheduler.sendExpiryReminders();

        verify(emailPort, never()).sendPlanExpiryReminder(any(), anyLong(), any());
        verify(userPlanRepository, never()).markExpiryReminderSent(anyLong(), any());
    }

    @Test
    void sendExpiryReminders_shouldQueryWith7DayWindow() {
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());

        Instant beforeThreshold = Instant.now().plus(7, ChronoUnit.DAYS);
        scheduler.sendExpiryReminders();
        Instant afterThreshold = Instant.now().plus(7, ChronoUnit.DAYS);

        verify(userPlanRepository).findPlansNeedingExpiryReminder(
                argThat(now -> !now.isAfter(Instant.now())),
                argThat(threshold -> !threshold.isBefore(beforeThreshold)
                        && !threshold.isAfter(afterThreshold)));
    }
}
