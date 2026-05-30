package com.huly.backend.domain.provider;

public interface PasswordHasher {
    boolean matches(String rawPassword, String encodedPassword);
    String encode(String rawPassword);
}
