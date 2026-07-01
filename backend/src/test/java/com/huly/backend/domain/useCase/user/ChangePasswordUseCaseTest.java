package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasherPort passwordHasherPort;

    private ChangePasswordUseCase changePasswordUseCase;

    private AppUser user;

    @BeforeEach
    void setUp() {
        changePasswordUseCase = new ChangePasswordUseCase(userRepository, passwordHasherPort);

        user = AppUser.builder()
                .id(1L)
                .email("user@huly.com")
                .password("encodedCurrentPass")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("currentPass", "encodedCurrentPass")).thenReturn(true);
        when(passwordHasherPort.encode("newPass123")).thenReturn("encodedNewPass");

        changePasswordUseCase.execute(1L, "currentPass", "newPass123");

        verify(userRepository).updatePassword(1L, "encodedNewPass");
    }

    @Test
    void execute_shouldEncodeNewPasswordBeforeSaving() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("currentPass", "encodedCurrentPass")).thenReturn(true);
        when(passwordHasherPort.encode("newPass123")).thenReturn("encodedNewPass");

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        changePasswordUseCase.execute(1L, "currentPass", "newPass123");

        verify(userRepository).updatePassword(org.mockito.ArgumentMatchers.eq(1L), passwordCaptor.capture());
        assertThat(passwordCaptor.getValue()).isEqualTo("encodedNewPass");
    }

    @Test
    void execute_shouldThrowInvalidCredentials_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> changePasswordUseCase.execute(99L, "currentPass", "newPass123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_shouldThrowInvalidCredentials_whenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrongPass", "encodedCurrentPass")).thenReturn(false);

        assertThatThrownBy(() -> changePasswordUseCase.execute(1L, "wrongPass", "newPass123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    void execute_shouldNeverCallUpdatePassword_whenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("wrongPass", "encodedCurrentPass")).thenReturn(false);

        assertThatThrownBy(() -> changePasswordUseCase.execute(1L, "wrongPass", "newPass123"))
                .isInstanceOf(InvalidCredentialsException.class);

        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never())
                .updatePassword(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
