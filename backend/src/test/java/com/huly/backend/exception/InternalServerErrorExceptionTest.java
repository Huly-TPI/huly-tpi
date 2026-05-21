package com.huly.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class InternalServerErrorExceptionTest {

    @Test
    void testInternalServerErrorExceptionWithMessage() {
        String message = "Internal server error";
        InternalServerErrorException exception = new InternalServerErrorException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInternalServerErrorExceptionWithMessageAndCause() {
        String message = "Internal server error";
        Throwable cause = new RuntimeException("Root cause");
        InternalServerErrorException exception = new InternalServerErrorException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = InternalServerErrorException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, annotation.value());
    }

    @Test
    void testInternalServerErrorExceptionIsRuntimeException() {
        InternalServerErrorException exception = new InternalServerErrorException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testInternalServerErrorExceptionWithNullMessage() {
        InternalServerErrorException exception = new InternalServerErrorException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testInternalServerErrorExceptionWithNullMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        InternalServerErrorException exception = new InternalServerErrorException(null, cause);

        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInternalServerErrorExceptionWithNullCause() {
        InternalServerErrorException exception = new InternalServerErrorException("test", null);
        assertEquals("test", exception.getMessage());
        assertNull(exception.getCause());
    }
}
