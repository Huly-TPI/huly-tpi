package com.huly.backend.domain.exception;

public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String fieldName, String fieldValue) {
        super(String.format("Ya existe un %s con %s %s", resource, fieldName, fieldValue));
    }
}
