package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    void execute_shouldReturnUser_whenEmailExists() {
        AppUser user = AppUser.builder()
                .id(1L).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));

        AppUser result = getCurrentUserUseCase.execute("user@huly.com");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Mili");
        assertThat(result.getEmail()).isEqualTo("user@huly.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.execute("ghost@huly.com"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found");
    }
}