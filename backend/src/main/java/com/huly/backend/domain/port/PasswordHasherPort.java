package com.huly.backend.domain.port;

public interface PasswordHasherPort {
    boolean matches(String rawPassword, String encodedPassword);
    String encode(String rawPassword);
}
