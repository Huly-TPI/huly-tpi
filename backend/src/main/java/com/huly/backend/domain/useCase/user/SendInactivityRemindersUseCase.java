package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.model.user.InactiveUserToRemind;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Envía (una sola vez por período de inactividad) el email recordatorio a los usuarios
 * que llevan {@code inactivityThresholdDays}+ días sin loguearse, invitándolos a volver.
 */
@Slf4j
@RequiredArgsConstructor
public class SendInactivityRemindersUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;
    private final EmailPort emailPort;
    private final Clock clock;
    private final int inactivityThresholdDays;
    private final int rewardCoins;

    public void execute() {
        Instant threshold = clock.instant().minus(Duration.ofDays(inactivityThresholdDays));
        List<InactiveUserToRemind> users = userDetailDomainRepository.findUsersNeedingInactivityReminder(threshold);
        log.info("[INACTIVITY] {} usuario(s) a recordar (umbral {} días)", users.size(), inactivityThresholdDays);

        for (InactiveUserToRemind user : users) {
            emailPort.sendInactivityReminder(user.email(), user.displayName(), rewardCoins);
            userDetailDomainRepository.markInactivityReminderSent(user.userId(), clock.instant());
        }
    }
}
