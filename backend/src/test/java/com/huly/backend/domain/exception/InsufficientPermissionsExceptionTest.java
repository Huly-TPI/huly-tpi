package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InsufficientPermissionsExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException("Acceso denegado");
        assertThat(ex.getMessage()).isEqualTo("Acceso denegado");
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
