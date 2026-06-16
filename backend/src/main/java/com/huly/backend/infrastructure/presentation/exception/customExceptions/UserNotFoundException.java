package com.huly.backend.infrastructure.presentation.exception.customExceptions;

import com.huly.backend.infrastructure.presentation.exception.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UUID id) {
        super("Usuario", "ID", id);
    }

    public UserNotFoundException(String email) {
        super("Usuario", "email", email);
    }
}
