package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        InfrastructureException ex = new InfrastructureException("Error de base de datos");
        assertThat(ex.getMessage()).isEqualTo("Error de base de datos");
    }

    @Test
    void messageAndCauseConstructor_shouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("timeout");
        InfrastructureException ex = new InfrastructureException("Error de conexión", cause);

        assertThat(ex.getMessage()).isEqualTo("Error de conexión");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        InfrastructureException ex = new InfrastructureException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        InfrastructureException ex = new InfrastructureException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
