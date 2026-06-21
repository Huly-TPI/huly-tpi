package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrantBadgeUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @InjectMocks private GrantBadgeUseCase grantBadgeUseCase;
    
    private AppUser user;
    private Badge badge;

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(1L)
                .email("user@huly.com")
               .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        badge = Badge.builder()
                .id(1L)
                .code("PRIMER_PASO")
                .name("Primer paso")
                .build();
    }

    @Test
    void execute_shouldGrantBadgeToUser_whenUserAndBadgeExistAndUserDoesNotHaveBadge() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(badgeRepository.findByCode("PRIMER_PASO")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeCode(1L, "PRIMER_PASO")).thenReturn(false);

        grantBadgeUseCase.execute("user@huly.com", "PRIMER_PASO");
        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void execute_shouldDoNothing_whenUserAlreadyHasBadge() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(badgeRepository.findByCode("PRIMER_PASO")).thenReturn(Optional.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeCode(1L, "PRIMER_PASO")).thenReturn(true);

        grantBadgeUseCase.execute("user@huly.com", "PRIMER_PASO");
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    @Test 
    void execute_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("noexiste@huly.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> grantBadgeUseCase.execute("noexiste@huly.com", "PRIMER_PASO")).isInstanceOf(NotFoundException.class);
    }

    @Test 
    void execute_shouldThrowNotFoundException_whenBadgeCodeDoesNotExist() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
       when(badgeRepository.findByCode("INEXISTENTE")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> grantBadgeUseCase.execute("user@huly.com", "INEXISTENTE")).isInstanceOf(NotFoundException.class);
    }

}
