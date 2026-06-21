package com.huly.backend.infrastructure.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailPortStubImplTest {

    private final EmailPortStubImpl emailPort = new EmailPortStubImpl();

    @Test
    void sendWelcomeLead_shouldNotThrow() {
        assertThatCode(() -> emailPort.sendWelcomeLead("user@example.com", "TestUser"))
                .doesNotThrowAnyException();
    }

     @Test
    void sendWelcomeLead_noLanzaExcepcion() {
        assertDoesNotThrow(() -> emailPort.sendWelcomeLead("user@example.com", "TestUser"));
    }

    @Test
    void sendReEngagement_noLanzaExcepcion() {
        assertDoesNotThrow(() -> emailPort.sendReEngagement("user@example.com", "tok-123"));
    }

    


}
