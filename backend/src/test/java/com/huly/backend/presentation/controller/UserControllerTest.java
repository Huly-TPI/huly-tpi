package com.huly.backend.presentation.controller;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.infrastructure.presentation.controller.UserController;
import com.huly.backend.infrastructure.presentation.dto.user.UserProfileResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private GetCurrentUserUseCase getCurrentUserUseCase;
    @Mock private UserDetailDomainRepository userDetailDomainRepository;

    @InjectMocks private UserController userController;

    private UserDetails principalWithId(Long id) {
        return new User(String.valueOf(id), "ignored", Collections.emptyList());
    }

    @Test
    void me_shouldReturnUserProfile_whenPrincipalIsValid() {
        AppUser user = AppUser.builder()
                .id(1L).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        when(getCurrentUserUseCase.execute(1L)).thenReturn(user);
        when(userDetailDomainRepository.findOnBoardingCompleted(1L)).thenReturn(Optional.of(true));
        when(userDetailDomainRepository.findOnboardingTutorialCompleted(1L)).thenReturn(Optional.of(false));

        ResponseEntity<UserProfileResponse> response =
                userController.me(principalWithId(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserProfileResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(1L);
        assertThat(body.getName()).isEqualTo("Mili");
        assertThat(body.getEmail()).isEqualTo("user@huly.com");
        assertThat(body.getRole()).isEqualTo(UserRole.USER);
        assertThat(body.getOnBoardingCompleted()).isTrue();
        assertThat(body.getOnboardingTutorialCompleted()).isFalse();
    }

    @Test
    void me_shouldThrowUnauthorized_whenPrincipalIsNull() {
        assertThatThrownBy(() -> userController.me(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Not authenticated");
    }
}