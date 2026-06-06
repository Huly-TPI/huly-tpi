package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateResourceExceptionTest {

    @Test
    void testMessageConstructor() {
        DuplicateResourceException ex = new DuplicateResourceException("El email ya existe");
        assertEquals("El email ya existe", ex.getMessage());
    }

    @Test
    void testFormattedMessageConstructor() {
        DuplicateResourceException ex = new DuplicateResourceException("Usuario", "email", "test@example.com");
        assertEquals("Ya existe un Usuario con email test@example.com", ex.getMessage());
    }

    @Test
    void testExtendsDomainException() {
        DuplicateResourceException ex = new DuplicateResourceException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        DuplicateResourceException ex = new DuplicateResourceException(null);
        assertNull(ex.getMessage());
    }
}
