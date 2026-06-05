package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testMessageConstructor() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario no encontrado");
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void testFormattedMessageConstructor() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", "id", 42);
        assertEquals("No se encontró un Usuario con id 42", ex.getMessage());
    }

    @Test
    void testExtendsDomainException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        assertNull(ex.getMessage());
    }
}
