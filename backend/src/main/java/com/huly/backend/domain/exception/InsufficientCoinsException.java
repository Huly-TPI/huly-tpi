package com.huly.backend.domain.exception;

public class InsufficientCoinsException extends BusinessRuleException {

        public InsufficientCoinsException(String message) {
            super(message);
        }
}
