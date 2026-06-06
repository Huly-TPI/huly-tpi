package com.huly.backend.exception;

import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {

    @Test
    void testUnauthorizedExceptionWithMessage() {
        String message = "Unauthorized error";
        UnauthorizedException exception = new UnauthorizedException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.UNAUTHORIZED, annotation.value());
    }

    @Test
    void testUnauthorizedExceptionIsRuntimeException() {
        UnauthorizedException exception = new UnauthorizedException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testUnauthorizedExceptionWithNullMessage() {
        UnauthorizedException exception = new UnauthorizedException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testUnauthorizedExceptionWithEmptyMessage() {
        String message = "";
        UnauthorizedException exception = new UnauthorizedException(message);
        assertEquals(message, exception.getMessage());
    }
}
