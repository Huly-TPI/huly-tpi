package com.huly.backend.infrastructure.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Exception e, HttpServletRequest request, Map<String, String> validationErrors) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .errors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(NotFoundException.class) // 404
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, e, request, null);
    }

    @ExceptionHandler(ConflictException.class) // 409
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e, HttpServletRequest request) {

        return buildResponse(HttpStatus.CONFLICT, e, request, null);
    }

    @ExceptionHandler(BadRequestException.class) // 400
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e, HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, e, request, null);
    }

    @ExceptionHandler(ForbiddenException.class) // 403
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {

        return buildResponse(HttpStatus.FORBIDDEN, e, request, null);
    }

    @ExceptionHandler(UnauthorizedException.class) //401
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED, e, request, null);
    }

    // Para DTOs
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e, HttpServletRequest request) {

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Exception("Ocurrió un error interno en el servidor"), request, null);
    }


}
