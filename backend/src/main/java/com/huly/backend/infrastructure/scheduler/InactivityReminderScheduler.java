package com.huly.backend.infrastructure.scheduler;

import com.huly.backend.domain.useCase.user.SendInactivityRemindersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job diario que dispara el envío de emails recordatorios a usuarios inactivos.
 * El cron es parametrizable vía {@code huly.inactivity.reminder-cron} (default: 10:00 AR).
 */
@Component
@RequiredArgsConstructor
public class InactivityReminderScheduler {

    private final SendInactivityRemindersUseCase sendInactivityRemindersUseCase;

    @Scheduled(cron = "${huly.inactivity.reminder-cron:0 0 10 * * *}", zone = "America/Argentina/Buenos_Aires")
    public void sendReminders() {
        sendInactivityRemindersUseCase.execute();
    }
}
