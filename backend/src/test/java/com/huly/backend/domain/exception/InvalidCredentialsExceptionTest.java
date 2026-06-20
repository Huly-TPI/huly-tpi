package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InvalidCredentialsExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciales inválidas");
        assertThat(ex.getMessage()).isEqualTo("Credenciales inválidas");
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
