package com.huly.backend.domain.port;

import java.time.Instant;

public interface EmailPort {
    void sendWelcomeLead(String to, String nickname);
    void sendReEngagement(String to, String unsubscribeToken);
    void sendPlanExpiryReminder(String to, long daysLeft, Instant expiresAt);
    void sendPasswordReset(String to, String token);
}
