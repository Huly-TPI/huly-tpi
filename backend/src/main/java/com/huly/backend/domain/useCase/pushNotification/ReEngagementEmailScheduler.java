package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReEngagementEmailScheduler {
    
    private final UserRepository userRepository;
    private final EmailPort emailPort;
    
    @Scheduled(cron = "0 0 23 * * *")
    public void sendReEngagementEmails() {
        Instant now = Instant.now();
        Instant start = now.minus(4, ChronoUnit.DAYS);
        Instant end = now.minus(3, ChronoUnit.DAYS);
        userRepository.findUsersInactiveBetween(start, end).forEach(user -> { 
            emailPort.sendReEngagement(user.getEmail(), user.getUnsubscribeToken());
        });
    }
}
