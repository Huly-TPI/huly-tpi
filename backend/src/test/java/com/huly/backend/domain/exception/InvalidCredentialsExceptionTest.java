package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void testMessageIsSet() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciales inválidas");
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void testExtendsDomainException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException(null);
        assertNull(ex.getMessage());
    }
}
