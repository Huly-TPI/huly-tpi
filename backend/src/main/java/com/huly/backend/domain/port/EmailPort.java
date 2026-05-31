package com.huly.backend.domain.port;

public interface EmailPort {
    void sendWelcomeLead(String to, String nickname);
}
