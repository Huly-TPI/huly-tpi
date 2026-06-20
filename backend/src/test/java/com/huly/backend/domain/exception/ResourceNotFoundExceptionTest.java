package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario no encontrado");
        assertThat(ex.getMessage()).isEqualTo("Usuario no encontrado");
    }

    @Test
    void formattedMessageConstructor_shouldSetFormattedMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", "id", 42);
        assertThat(ex.getMessage()).isEqualTo("No se encontró un Usuario con id 42");
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
