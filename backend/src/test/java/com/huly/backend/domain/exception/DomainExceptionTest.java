package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    private static class ConcreteDomainException extends DomainException {
        ConcreteDomainException(String message) { super(message); }
        ConcreteDomainException(String message, Throwable cause) { super(message, cause); }
    }

    @Test
    void messageConstructor_shouldSetMessage() {
        DomainException ex = new ConcreteDomainException("error de dominio");
        assertThat(ex.getMessage()).isEqualTo("error de dominio");
    }

    @Test
    void messageAndCauseConstructor_shouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("causa");
        DomainException ex = new ConcreteDomainException("error de dominio", cause);

        assertThat(ex.getMessage()).isEqualTo("error de dominio");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void inheritance_shouldExtendRuntimeException() {
        DomainException ex = new ConcreteDomainException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        DomainException ex = new ConcreteDomainException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
