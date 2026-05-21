package com.huly.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String resource, String fieldName, String fieldValue) {
        super(String.format("Ya existe en %s con %s %s", resource, fieldName, fieldValue));
    }

}
