package com.huly.backend.infrastructure.presentation.exception;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.exception.InfrastructureException;
import com.huly.backend.domain.exception.InsufficientPermissionsException;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.exception.AccountNotActiveException;
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

    // --- Domain exceptions ---

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, e, request, null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, e, request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, e, request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, e, request, null);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissionsException(InsufficientPermissionsException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, e, request, null);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleInfrastructureException(InfrastructureException e, HttpServletRequest request) {
        log.error("Infrastructure error: {}", e.getMessage(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Exception("Ocurrió un error interno en el servidor"), request, null);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActiveException(
            AccountNotActiveException e,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED, e, request, null);
    }

    // --- Infrastructure/presentation exceptions ---

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
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Exception("Ocurrió un error interno en el servidor"), request, null);
    }
}
