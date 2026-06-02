package com.huly.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador global de excepciones para toda la API REST.
 * @RestControllerAdvice intercepta excepciones lanzadas en cualquier @RestController
 * y las convierte en respuestas HTTP con el código de estado y body JSON apropiados.
 *
 * Mapeo de excepciones a códigos HTTP:
 *  - NotFoundException → 404 Not Found
 *  - ConflictException → 409 Conflict (ej: email duplicado)
 *  - BadRequestException → 400 Bad Request (ej: validación de dominio)
 *  - ForbiddenException → 403 Forbidden (ej: sin permisos)
 *  - UnauthorizedException → 401 Unauthorized (ej: credenciales inválidas)
 *  - MethodArgumentNotValidException → 400 con mapa de errores por campo (validación Bean Validation)
 *  - Exception (catch-all) → 500 Internal Server Error (mensaje genérico, sin detalles internos)
 *
 * Cada respuesta incluye un traceId UUID para facilitar el debugging en logs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Construye la respuesta de error estándar de la API.
     * El traceId permite correlacionar el error del cliente con los logs del servidor.
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Exception e, HttpServletRequest request, Map<String, String> validationErrors) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString()) // ID único para correlacionar con logs
                .errors(validationErrors)              // Solo presente en errores de validación
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, e, request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, e, request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, e, request, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, e, request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, e, request, null);
    }

    /**
     * Maneja errores de validación de Bean Validation (@Valid en DTOs).
     * Extrae todos los errores de campo y los agrupa en un mapa {campo: mensaje}
     * para que el cliente pueda mostrar errores específicos por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, new Exception("Error de validación en los datos enviados"), request, errors);
    }

    /**
     * Catch-all para cualquier excepción no manejada explícitamente.
     * Devuelve un mensaje genérico (500) sin exponer detalles internos del servidor al cliente.
     * NOTA: sería bueno agregar log.error() aquí para no perder el stack trace en producción.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Exception("Ocurrió un error interno en el servidor"), request, null);
    }
}
