package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;

    @InjectMocks private RegisterUseCase registerUseCase;

    @Test
    void execute_shouldSaveUserWithNameRoleUserAndStatusActive() {
        when(userRepository.existsByEmail("new@huly.com")).thenReturn(false);
        when(passwordHasher.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(1L).build());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        registerUseCase.execute("new@huly.com", "rawPass", "Juan");

        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Juan");
        assertThat(saved.getEmail()).isEqualTo("new@huly.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPass");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void execute_shouldThrowConflictException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@huly.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.execute("existing@huly.com", "rawPass", "Juan"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void execute_shouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail("new@huly.com")).thenReturn(false);
        when(passwordHasher.encode("rawPass")).thenReturn("hashedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().build());

        registerUseCase.execute("new@huly.com", "rawPass", "Juan");

        verify(passwordHasher).encode("rawPass");
    }
}
