package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsufficientPermissionsExceptionTest {

    @Test
    void testMessageIsSet() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException("Acceso denegado");
        assertEquals("Acceso denegado", ex.getMessage());
    }

    @Test
    void testExtendsDomainException() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        InsufficientPermissionsException ex = new InsufficientPermissionsException(null);
        assertNull(ex.getMessage());
    }
}
