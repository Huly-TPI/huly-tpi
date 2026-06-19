package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private LoginUseCase loginUseCase;

    @InjectMocks private RegisterUseCase registerUseCase;

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);

    @Test
    void execute_shouldSaveUserWithNameRoleUserAndStatusActive() {
        when(userRepository.existsByEmail("new@huly.com")).thenReturn(false);
        when(passwordHasherPort.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().id(1L).build());
        when(loginUseCase.execute("new@huly.com", "rawPass")).thenReturn(AuthTokens.builder().build());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        registerUseCase.execute("new@huly.com", "rawPass", "Juan", BIRTH_DATE);

        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Juan");
        assertThat(saved.getEmail()).isEqualTo("new@huly.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPass");
        assertThat(saved.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void execute_shouldThrowConflictException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@huly.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.execute("existing@huly.com", "rawPass", "Juan", BIRTH_DATE))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void execute_shouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail("new@huly.com")).thenReturn(false);
        when(passwordHasherPort.encode("rawPass")).thenReturn("hashedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().build());
        when(loginUseCase.execute("new@huly.com", "rawPass")).thenReturn(AuthTokens.builder().build());

        registerUseCase.execute("new@huly.com", "rawPass", "Juan", BIRTH_DATE);

        verify(passwordHasherPort).encode("rawPass");
    }

    @Test
    void execute_shouldReturnTokensFromLogin() {
        AuthTokens expected = AuthTokens.builder()
                .accessToken("tok").refreshToken("ref").role(UserRole.USER).build();
        when(userRepository.existsByEmail("new@huly.com")).thenReturn(false);
        when(passwordHasherPort.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().build());
        when(loginUseCase.execute("new@huly.com", "rawPass")).thenReturn(expected);

        AuthTokens result = registerUseCase.execute("new@huly.com", "rawPass", "Juan", BIRTH_DATE);

        assertThat(result).isEqualTo(expected);
    }
}