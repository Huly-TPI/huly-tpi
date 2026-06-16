package com.huly.backend.exception;

import com.huly.backend.infrastructure.presentation.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class ConflictExceptionTest {

    @Test
    void testConflictExceptionWithMessage() {
        String message = "Conflict error";
        ConflictException exception = new ConflictException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConflictExceptionWithResourceFieldAndValue() {
        ConflictException exception = new ConflictException("Usuario", "email", "test@example.com");

        assertEquals("Ya existe en Usuario con email test@example.com", exception.getMessage());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = ConflictException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.CONFLICT, annotation.value());
    }

    @Test
    void testConflictExceptionIsRuntimeException() {
        ConflictException exception = new ConflictException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConflictExceptionWithNullValues() {
        ConflictException exception = new ConflictException(null, null, null);
        assertEquals("Ya existe en null con null null", exception.getMessage());
    }

    @Test
    void testConflictExceptionWithEmptyValues() {
        ConflictException exception = new ConflictException("", "", "");
        assertEquals("Ya existe en  con  ", exception.getMessage());
    }
}
