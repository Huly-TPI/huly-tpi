package com.huly.backend.domain.exception;

public class AccountNotActiveException extends DomainException {

    public AccountNotActiveException(String message) {
        super(message);
    }
}