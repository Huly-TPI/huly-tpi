package com.huly.backend.infrastructure.email;

import org.springframework.stereotype.Component;

import com.huly.backend.domain.port.EmailPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailPortStubImpl implements EmailPort {

    @Override
    public void sendWelcomeLead(String to, String nickname) {
        log.info("[EMAIL-STUB] Bienvenida lead → to={} nickname={}", to, nickname);
    }

    @Override
    public void sendReEngagement(String to, String unsubscribeToken) {
        log.info("[EMAIL-STUB] Re-engagement → to={}", to, unsubscribeToken);
    }
}
