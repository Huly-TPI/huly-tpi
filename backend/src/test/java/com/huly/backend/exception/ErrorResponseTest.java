package com.huly.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.infrastructure.presentation.exception.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testErrorResponseBuilder() {
        Instant now = Instant.now();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Error message")
                .path("/api/test")
                .traceId("trace-123")
                .build();

        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Error message", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertEquals("trace-123", errorResponse.getTraceId());
        assertNull(errorResponse.getErrors());
        assertNull(errorResponse.getDebugMessage());
    }

    @Test
    void testErrorResponseWithAllFields() {
        Instant now = Instant.now();
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "error1");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Error message")
                .path("/api/test")
                .traceId("trace-123")
                .errors(errors)
                .debugMessage("Debug info")
                .build();

        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Error message", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertEquals("trace-123", errorResponse.getTraceId());
        assertEquals(errors, errorResponse.getErrors());
        assertEquals("Debug info", errorResponse.getDebugMessage());
    }

    @Test
    void testErrorResponseNoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNull(errorResponse.getTimestamp());
        assertNull(errorResponse.getStatus());
        assertNull(errorResponse.getError());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getTraceId());
        assertNull(errorResponse.getErrors());
        assertNull(errorResponse.getDebugMessage());
    }

    @Test
    void testErrorResponseAllArgsConstructor() {
        Instant now = Instant.now();
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "error1");

        ErrorResponse errorResponse = new ErrorResponse(
                now, 400, "Bad Request", "Error message",
                "/api/test", "trace-123", errors, "Debug info"
        );

        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Error message", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertEquals("trace-123", errorResponse.getTraceId());
        assertEquals(errors, errorResponse.getErrors());
        assertEquals("Debug info", errorResponse.getDebugMessage());
    }

    @Test
    void testErrorResponseSetters() {
        ErrorResponse errorResponse = new ErrorResponse();
        Instant now = Instant.now();

        errorResponse.setTimestamp(now);
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        errorResponse.setMessage("Server error");
        errorResponse.setPath("/api/endpoint");
        errorResponse.setTraceId("trace-456");
        errorResponse.setDebugMessage("Stack trace");

        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Server error", errorResponse.getMessage());
        assertEquals("/api/endpoint", errorResponse.getPath());
        assertEquals("trace-456", errorResponse.getTraceId());
        assertEquals("Stack trace", errorResponse.getDebugMessage());
    }

    @Test
    void testErrorResponseWithNullErrors() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .build();

        assertNull(errorResponse.getErrors());
    }

    @Test
    void testErrorResponseWithEmptyErrors() {
        Map<String, String> errors = new HashMap<>();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .errors(errors)
                .build();

        assertTrue(errorResponse.getErrors().isEmpty());
    }
}
