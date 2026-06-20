package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserBadgesUseCaseTest {
    @Mock
    private UserBadgeRepository userBadgeRepository;
    @InjectMocks
    private GetUserBadgesUseCase getUserBadgesUseCase;
    private UserBadge userBadge;

    @BeforeEach
    void setUp() {
        userBadge = UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build())
                .obtainedAt(Instant.now())
                .build();
    }

    @Test
    void execute_shouldReturnUserBadges_whenUserExists() {
        when(userBadgeRepository.findAllByUserId(1L)).thenReturn(List.of(userBadge));

        List<UserBadge> result = getUserBadgesUseCase.execute(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBadge().getCode()).isEqualTo("PRIMER_PASO");
        verify(userBadgeRepository).findAllByUserId(1L);
    }

    @Test
    void execute_shouldReturnEmptyList_whenUserHasNoBadges() {
        when(userBadgeRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<UserBadge> result = getUserBadgesUseCase.execute(1L);
        assertThat(result).isEmpty();
    }

}
