package com.huly.backend.exception.customExceptions;

import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.presentation.exception.customExceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void testUserNotFoundExceptionWithUuid() {
        UUID id = UUID.randomUUID();
        UserNotFoundException exception = new UserNotFoundException(id);

        assertEquals(String.format("No se encontro un Usuario con ID %s", id), exception.getMessage());
    }

    @Test
    void testUserNotFoundExceptionWithEmail() {
        String email = "test@example.com";
        UserNotFoundException exception = new UserNotFoundException(email);

        assertEquals("No se encontro un Usuario con email test@example.com", exception.getMessage());
    }

    @Test
    void testUserNotFoundExceptionIsNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("test@example.com");
        assertInstanceOf(NotFoundException.class, exception);
    }

    @Test
    void testUserNotFoundExceptionIsRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException(UUID.randomUUID());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testUserNotFoundExceptionWithNullEmail() {
        UserNotFoundException exception = new UserNotFoundException((String) null);
        assertEquals("No se encontro un Usuario con email null", exception.getMessage());
    }
}
