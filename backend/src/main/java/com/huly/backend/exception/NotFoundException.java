package com.huly.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resource, String fieldName, Object fieldValue) {
        super(String.format("No se encontro un %s con %s %s", resource, fieldName, fieldValue));
    }

}
