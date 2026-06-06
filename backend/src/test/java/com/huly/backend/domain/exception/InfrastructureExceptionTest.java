package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfrastructureExceptionTest {

    @Test
    void testMessageConstructor() {
        InfrastructureException ex = new InfrastructureException("Error de base de datos");
        assertEquals("Error de base de datos", ex.getMessage());
    }

    @Test
    void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("timeout");
        InfrastructureException ex = new InfrastructureException("Error de conexión", cause);

        assertEquals("Error de conexión", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testExtendsDomainException() {
        InfrastructureException ex = new InfrastructureException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        InfrastructureException ex = new InfrastructureException(null);
        assertNull(ex.getMessage());
    }
}
