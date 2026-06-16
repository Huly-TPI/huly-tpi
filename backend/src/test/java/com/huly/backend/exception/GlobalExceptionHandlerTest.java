package com.huly.backend.exception;

import com.huly.backend.domain.exception.*;
import com.huly.backend.infrastructure.presentation.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    // --- Domain exception handlers ---

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Usuario no encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTraceId());
    }

    @Test
    void testHandleResourceNotFoundExceptionWithFormattedMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Usuario", "id", 99);

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No se encontró un Usuario con id 99", response.getBody().getMessage());
    }

    @Test
    void testHandleInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Credenciales inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialsException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Credenciales inválidas", response.getBody().getMessage());
    }

    @Test
    void testHandleDuplicateResourceException() {
        DuplicateResourceException exception = new DuplicateResourceException("El email ya existe");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("El email ya existe", response.getBody().getMessage());
    }

    @Test
    void testHandleDuplicateResourceExceptionWithFormattedMessage() {
        DuplicateResourceException exception = new DuplicateResourceException("Usuario", "email", "test@example.com");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ya existe un Usuario con email test@example.com", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessRuleException() {
        BusinessRuleException exception = new BusinessRuleException("source es obligatorio");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("source es obligatorio", response.getBody().getMessage());
    }

    @Test
    void testHandleInsufficientPermissionsException() {
        InsufficientPermissionsException exception = new InsufficientPermissionsException("Acceso denegado");

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientPermissionsException(exception, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Acceso denegado", response.getBody().getMessage());
    }

    @Test
    void testHandleInfrastructureException() {
        InfrastructureException exception = new InfrastructureException("Error de base de datos");

        ResponseEntity<ErrorResponse> response = handler.handleInfrastructureException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Ocurrió un error interno en el servidor", response.getBody().getMessage());
    }

    @Test
    void testDomainExceptionTraceIdIsUnique() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("test 1");
        ResourceNotFoundException ex2 = new ResourceNotFoundException("test 2");

        ResponseEntity<ErrorResponse> r1 = handler.handleResourceNotFoundException(ex1, request);
        ResponseEntity<ErrorResponse> r2 = handler.handleResourceNotFoundException(ex2, request);

        assertNotEquals(r1.getBody().getTraceId(), r2.getBody().getTraceId());
    }

    @Test
    void testHandleInvalidGoalImageException() {
        InvalidGoalImageException exception = new InvalidGoalImageException("La imagen no corresponde al reto");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidGoalImageException(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Unprocessable Entity", response.getBody().getError());
        assertEquals("La imagen no corresponde al reto", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTraceId());
    }

    @Test
    void testHandleImageValidationUnavailableException() {
        ImageValidationUnavailableException exception = new ImageValidationUnavailableException(
                "Servicio no disponible", new RuntimeException("timeout")
        );

        ResponseEntity<ErrorResponse> response = handler.handleImageValidationUnavailableException(exception, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Service Unavailable", response.getBody().getError());
        assertEquals(
                "El servicio de validación de imágenes no está disponible. Intentá de nuevo más tarde.",
                response.getBody().getMessage()
        );
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(5L * 1024 * 1024);

        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceededException(exception, request);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(413, response.getBody().getStatus());
        assertEquals("Payload Too Large", response.getBody().getError());
        assertEquals("La imagen no puede superar los 5 MB.", response.getBody().getMessage());
    }
}
