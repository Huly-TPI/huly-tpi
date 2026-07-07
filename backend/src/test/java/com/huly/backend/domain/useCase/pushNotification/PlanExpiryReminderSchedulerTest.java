package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
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

    private static final String USER_EMAIL = "user@huly";

    @Mock
    private UserPlanRepository userPlanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private PlanExpiryReminderScheduler scheduler;

    private Long planId;
    private Instant planExpiresAt;
    private Instant thresholdLowerBound;
    private Instant thresholdUpperBound;

    @Test
    @DisplayName("Envía el aviso y marca el recordatorio cuando el plan está por vencer")
    void sendExpiryRemindersShouldSendAndMarkWhenPlanIsExpiringSoon() {
        // --- arrange ---
        givenPlanNeedingReminder(10L, 1L, 5);
        givenUserExists(1L, USER_EMAIL);
        // --- act ---
        sendExpiryReminders();
        // --- assert ---
        thenReminderSentAndMarked(USER_EMAIL);
    }

    @Test
    @DisplayName("No hace nada cuando ningún plan necesita recordatorio")
    void sendExpiryRemindersShouldDoNothingWhenNoPlansNeedReminder() {
        // --- arrange ---
        givenNoPlansNeedingReminder();
        // --- act ---
        sendExpiryReminders();
        // --- assert ---
        thenNoReminderSentNorMarked();
    }

    @Test
    @DisplayName("No envía ni marca cuando el usuario del plan no existe")
    void sendExpiryRemindersShouldNotSendNorMarkWhenUserMissing() {
        // --- arrange ---
        givenPlanNeedingReminder(10L, 99L, 3);
        givenUserMissing(99L);
        // --- act ---
        sendExpiryReminders();
        // --- assert ---
        thenNoReminderSentNorMarked();
    }

    @Test
    @DisplayName("Consulta con una ventana de 7 días")
    void sendExpiryRemindersShouldQueryWithSevenDayWindow() {
        // --- arrange ---
        givenNoPlansNeedingReminder();
        // --- act ---
        sendExpiryReminders();
        // --- assert ---
        thenQueriedWithSevenDayWindow();
    }

    // --- arrange ---

    private AppUser user(long id, String email) {
        return AppUser.builder()
                .id(id).email(email)
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
    }

    private void givenPlanNeedingReminder(long planId, long userId, int daysToExpiry) {
        this.planId = planId;
        this.planExpiresAt = Instant.now().plus(daysToExpiry, ChronoUnit.DAYS);
        UserPlan plan = UserPlan.builder()
                .id(planId).userId(userId).planCode("PREMIUM").expiresAt(planExpiresAt)
                .build();
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(plan));
    }

    private void givenNoPlansNeedingReminder() {
        when(userPlanRepository.findPlansNeedingExpiryReminder(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());
    }

    private void givenUserExists(long userId, String email) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, email)));
    }

    private void givenUserMissing(long userId) {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private void sendExpiryReminders() {
        thresholdLowerBound = Instant.now().plus(7, ChronoUnit.DAYS);
        scheduler.sendExpiryReminders();
        thresholdUpperBound = Instant.now().plus(7, ChronoUnit.DAYS);
    }

    // --- assert ---

    private void thenReminderSentAndMarked(String email) {
        verify(emailPort).sendPlanExpiryReminder(eq(email), anyLong(), eq(planExpiresAt));
        verify(userPlanRepository).markExpiryReminderSent(planId, planExpiresAt);
    }

    private void thenNoReminderSentNorMarked() {
        verify(emailPort, never()).sendPlanExpiryReminder(any(), anyLong(), any());
        verify(userPlanRepository, never()).markExpiryReminderSent(anyLong(), any());
    }

    private void thenQueriedWithSevenDayWindow() {
        verify(userPlanRepository).findPlansNeedingExpiryReminder(
                argThat(now -> !now.isAfter(Instant.now())),
                argThat(threshold -> !threshold.isBefore(thresholdLowerBound)
                        && !threshold.isAfter(thresholdUpperBound)));
    }
}
