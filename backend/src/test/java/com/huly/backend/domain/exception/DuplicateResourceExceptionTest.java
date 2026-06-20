package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DuplicateResourceExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        DuplicateResourceException ex = new DuplicateResourceException("El email ya existe");
        assertThat(ex.getMessage()).isEqualTo("El email ya existe");
    }

    @Test
    void formattedMessageConstructor_shouldSetFormattedMessage() {
        DuplicateResourceException ex = new DuplicateResourceException("Usuario", "email", "test@example.com");
        assertThat(ex.getMessage()).isEqualTo("Ya existe un Usuario con email test@example.com");
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        DuplicateResourceException ex = new DuplicateResourceException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        DuplicateResourceException ex = new DuplicateResourceException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
