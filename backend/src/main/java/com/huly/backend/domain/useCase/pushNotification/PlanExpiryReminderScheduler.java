package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Job diario que avisa por mail a los usuarios cuyo plan vence en 7 días o menos.
 * Solo se envía un mail por cada expiración: tras avisar se marca
 * {@code expiry_reminder_sent_for = expires_at}, y al renovar (cambia expires_at) se reactiva.
 * El filtro de baja de emails y la ventana de vencimiento se aplican en la consulta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanExpiryReminderScheduler {

    private static final int REMINDER_WINDOW_DAYS = 7;

    private final UserPlanRepository userPlanRepository;
    private final UserRepository userRepository;
    private final EmailPort emailPort;
//tt
    @Scheduled(cron = "0 0 23 * * *", zone = "America/Argentina/Buenos_Aires")
    public void sendExpiryReminders() {
        Instant now = Instant.now();
        Instant threshold = now.plus(REMINDER_WINDOW_DAYS, ChronoUnit.DAYS);
        for (UserPlan plan : userPlanRepository.findPlansNeedingExpiryReminder(now, threshold)) {
            userRepository.findById(plan.getUserId()).ifPresent(user -> {
                long daysLeft = (long) Math.ceil(
                        (double) Duration.between(now, plan.getExpiresAt()).toHours() / 24);
                emailPort.sendPlanExpiryReminder(user.getEmail(), daysLeft, plan.getExpiresAt());
                userPlanRepository.markExpiryReminderSent(plan.getId(), plan.getExpiresAt());
            });
        }
    }
}
