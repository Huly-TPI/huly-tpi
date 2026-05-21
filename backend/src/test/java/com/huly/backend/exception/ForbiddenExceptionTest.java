package com.huly.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExceptionTest {

    @Test
    void testForbiddenExceptionWithMessage() {
        String message = "Forbidden error";
        ForbiddenException exception = new ForbiddenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = ForbiddenException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.FORBIDDEN, annotation.value());
    }

    @Test
    void testForbiddenExceptionIsRuntimeException() {
        ForbiddenException exception = new ForbiddenException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testForbiddenExceptionWithNullMessage() {
        ForbiddenException exception = new ForbiddenException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testForbiddenExceptionWithEmptyMessage() {
        String message = "";
        ForbiddenException exception = new ForbiddenException(message);
        assertEquals(message, exception.getMessage());
    }
}
