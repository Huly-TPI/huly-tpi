package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

    private static class ConcreteDomainException extends DomainException {
        ConcreteDomainException(String message) { super(message); }
        ConcreteDomainException(String message, Throwable cause) { super(message, cause); }
    }

    @Test
    void testMessageIsSet() {
        DomainException ex = new ConcreteDomainException("error de dominio");
        assertEquals("error de dominio", ex.getMessage());
    }

    @Test
    void testMessageAndCauseAreSet() {
        Throwable cause = new RuntimeException("causa");
        DomainException ex = new ConcreteDomainException("error de dominio", cause);

        assertEquals("error de dominio", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testIsDomainException() {
        DomainException ex = new ConcreteDomainException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void testNullMessage() {
        DomainException ex = new ConcreteDomainException(null);
        assertNull(ex.getMessage());
    }
}
