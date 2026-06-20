package com.huly.backend.domain.port;

public interface EmailPort {
    void sendWelcomeLead(String to, String nickname);

    /**
     * Email recordatorio para usuarios inactivos, invitándolos a volver.
     *
     * @param rewardCoins monedas de regalo que se mencionan como incentivo.
     */
    void sendInactivityReminder(String to, String displayName, int rewardCoins);
}
