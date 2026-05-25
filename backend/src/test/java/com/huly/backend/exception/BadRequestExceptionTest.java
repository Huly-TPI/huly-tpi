package com.huly.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void testBadRequestExceptionWithMessage() {
        String message = "Bad request error";
        BadRequestException exception = new BadRequestException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = BadRequestException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
    }

    @Test
    void testBadRequestExceptionIsRuntimeException() {
        BadRequestException exception = new BadRequestException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testBadRequestExceptionWithNullMessage() {
        BadRequestException exception = new BadRequestException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testBadRequestExceptionWithEmptyMessage() {
        String message = "";
        BadRequestException exception = new BadRequestException(message);
        assertEquals(message, exception.getMessage());
    }
}
