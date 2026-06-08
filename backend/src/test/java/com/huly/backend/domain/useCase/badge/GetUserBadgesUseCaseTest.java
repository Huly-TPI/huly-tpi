package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.UserBadgeRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserBadgesUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @InjectMocks private GetUserBadgesUseCase getUserBadgesUseCase;

    private AppUser user;
    private UserBadge userBadge; 

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(1L)
                .email("user@huly.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userBadge = UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build())
                .obtainedAt(Instant.now())
                .build();
                 }

    @Test
    void execute_shouldReturnUserBadges_whenUserExists() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(userBadgeRepository.findAllByUserId(1L)).thenReturn(List.of(userBadge));

        List<UserBadge> result = getUserBadgesUseCase.execute("user@huly.com");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadge().getCode()).isEqualTo("PRIMER_PASO");
        verify(userBadgeRepository).findAllByUserId(1L);
    }

    @Test
    void execute_shouldReturnEmptyList_whenUserHasNoBadges() {
        when(userRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(user));
        when(userBadgeRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<UserBadge> result = getUserBadgesUseCase.execute("user@huly.com");
        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findByEmail("noexiste@huly.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> getUserBadgesUseCase.execute("noexiste@huly.com")).isInstanceOf(NotFoundException.class);
    }

}


