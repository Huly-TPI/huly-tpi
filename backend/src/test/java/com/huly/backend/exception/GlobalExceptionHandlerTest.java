package com.huly.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void testHandleNotFoundException() {
        NotFoundException exception = new NotFoundException("Usuario no encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTraceId());
        assertNull(response.getBody().getErrors());
    }

    @Test
    void testHandleConflictException() {
        ConflictException exception = new ConflictException("Usuario", "email", "test@example.com");

        ResponseEntity<ErrorResponse> response = handler.handleConflictException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Ya existe en Usuario con email test@example.com", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTraceId());
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException exception = new BadRequestException("Bad request");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequestException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Bad request", response.getBody().getMessage());
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleForbiddenException(exception, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException("Not authenticated");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Not authenticated", response.getBody().getMessage());
    }


    @Test
    void testHandleGlobalException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Ocurrió un error interno en el servidor", response.getBody().getMessage());
    }

    @Test
    void testErrorResponseContainsTimestamp() {
        NotFoundException exception = new NotFoundException("Test");

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(exception, request);

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponseContainsUniqueTraceId() {
        NotFoundException exception1 = new NotFoundException("Test 1");
        NotFoundException exception2 = new NotFoundException("Test 2");

        ResponseEntity<ErrorResponse> response1 = handler.handleNotFoundException(exception1, request);
        ResponseEntity<ErrorResponse> response2 = handler.handleNotFoundException(exception2, request);

        assertNotEquals(response1.getBody().getTraceId(), response2.getBody().getTraceId());
    }

    @Test
    void testHandleNotFoundExceptionWithFormattedMessage() {
        NotFoundException exception = new NotFoundException("Usuario", "id", 123);

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No se encontro un Usuario con id 123", response.getBody().getMessage());
    }

    @Test
    void testHandleConflictExceptionWithFormattedMessage() {
        ConflictException exception = new ConflictException("Producto", "codigo", "ABC123");

        ResponseEntity<ErrorResponse> response = handler.handleConflictException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ya existe en Producto con codigo ABC123", response.getBody().getMessage());
    }
}
