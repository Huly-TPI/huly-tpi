package com.huly.backend.infrastructure.email;

import org.springframework.stereotype.Component;

import com.huly.backend.domain.port.EmailPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailPortStubImpl implements EmailPort {

    @Override
    public void sendWelcomeLead(String to, String nickname) {
        // TODO: implementar con proveedor de email Brevo
        log.info("[EMAIL-STUB] Bienvenida lead → to={} nickname={}", to, nickname);
    }
}
