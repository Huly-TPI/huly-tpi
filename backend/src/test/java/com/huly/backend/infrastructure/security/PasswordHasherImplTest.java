package com.huly.backend.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHasherImplTest {

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PasswordHasherImpl passwordHasher;

    @Test
    void matches_shouldReturnTrue_whenPasswordMatches() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        assertThat(passwordHasher.matches("raw", "encoded")).isTrue();
    }

    @Test
    void matches_shouldReturnFalse_whenPasswordDoesNotMatch() {
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThat(passwordHasher.matches("wrong", "encoded")).isFalse();
    }

    @Test
    void encode_shouldDelegateToPasswordEncoder() {
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

        String result = passwordHasher.encode("rawPassword");

        assertThat(result).isEqualTo("hashedPassword");
        verify(passwordEncoder).encode("rawPassword");
    }
}
