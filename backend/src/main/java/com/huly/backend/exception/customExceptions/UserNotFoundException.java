package com.huly.backend.exception.customExceptions;

import com.huly.backend.exception.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UUID id) {
        super("Usuario", "ID", id);
    }

    public UserNotFoundException(String email) {
        super("Usuario", "email", email);
    }
}
