package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.dto.badge.GetUserBadgesRequest;
import com.huly.backend.domain.dto.badge.GetUserBadgesResponse;
import com.huly.backend.domain.mapper.badge.GetUserBadgesMapper;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserBadgesUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final String BADGE_CODE = "PRIMER_PASO";

    @Mock
    private UserBadgeRepository userBadgeRepository;

    private GetUserBadgesUseCase getUserBadgesUseCase;

    private UserBadge userBadge;

    @BeforeEach
    void setUp() {
        getUserBadgesUseCase = new GetUserBadgesUseCase(userBadgeRepository, new GetUserBadgesMapper());
        userBadge = UserBadge.builder()
                .id(1L)
                .userId(USER_ID)
                .badge(Badge.builder().id(1L).code(BADGE_CODE).name("Primer paso").build())
                .obtainedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Devuelve las insignias del usuario cuando el usuario tiene insignias")
    void executeShouldReturnUserBadgesWhenUserExists() {
        givenUserWithBadges();

        GetUserBadgesResponse result = getUserBadges();

        thenBadgesReturned(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el usuario no tiene insignias")
    void executeShouldReturnEmptyListWhenUserHasNoBadges() {
        givenUserWithoutBadges();

        GetUserBadgesResponse result = getUserBadges();

        thenNoBadgesReturned(result);
    }

    // --- arrange ---

    private void givenUserWithBadges() {
        when(userBadgeRepository.findAllByUserId(USER_ID)).thenReturn(List.of(userBadge));
    }

    private void givenUserWithoutBadges() {
        when(userBadgeRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
    }

    // --- act ---

    private GetUserBadgesResponse getUserBadges() {
        return getUserBadgesUseCase.execute(new GetUserBadgesRequest(USER_ID));
    }

    // --- assert ---

    private void thenBadgesReturned(GetUserBadgesResponse result) {
        assertThat(result.badges()).hasSize(1);
        assertThat(result.badges().get(0).badge().code()).isEqualTo(BADGE_CODE);
        verify(userBadgeRepository).findAllByUserId(USER_ID);
    }

    private void thenNoBadgesReturned(GetUserBadgesResponse result) {
        assertThat(result.badges()).isEmpty();
    }
}
