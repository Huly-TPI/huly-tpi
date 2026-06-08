package com.huly.backend.infrastructure.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailPortStubImplTest {

    private final EmailPortStubImpl emailPort = new EmailPortStubImpl();

    @Test
    void sendWelcomeLead_shouldNotThrow() {
        assertThatCode(() -> emailPort.sendWelcomeLead("user@example.com", "TestUser"))
                .doesNotThrowAnyException();
    }
}
