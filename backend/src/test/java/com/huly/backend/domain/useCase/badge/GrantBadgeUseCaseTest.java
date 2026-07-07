package com.huly.backend.domain.useCase.badge;

import com.huly.backend.domain.dto.badge.GrantBadgeRequest;
import com.huly.backend.domain.dto.badge.GrantBadgeResponse;
import com.huly.backend.domain.mapper.badge.GrantBadgeMapper;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import com.huly.backend.domain.repository.user.UserBadgeRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrantBadgeUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@huly.com";
    private static final String BADGE_CODE = "PRIMER_PASO";
    private static final String UNKNOWN_EMAIL = "noexiste@huly.com";
    private static final String UNKNOWN_BADGE_CODE = "INEXISTENTE";

    @Mock
    private UserRepository userRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    private GrantBadgeUseCase grantBadgeUseCase;

    private AppUser user;
    private Badge badge;

    @BeforeEach
    void setUp() {
        grantBadgeUseCase = new GrantBadgeUseCase(badgeRepository, userBadgeRepository, userRepository,
                new GrantBadgeMapper());
        user = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        badge = Badge.builder()
                .id(1L)
                .code(BADGE_CODE)
                .name("Primer paso")
                .build();
    }

    @Test
    @DisplayName("Otorga la insignia y la persiste cuando el usuario y la insignia existen y el usuario aún no la tiene")
    void executeShouldGrantBadgeWhenUserAndBadgeExistAndUserDoesNotHaveBadge() {
        givenExistingUser();
        givenExistingBadge();
        givenUserDoesNotHaveBadge();

        GrantBadgeResponse result = grantBadge();

        thenBadgeGrantedAndSaved(result);
    }

    @Test
    @DisplayName("No otorga ni persiste la insignia cuando el usuario ya la posee")
    void executeShouldNotGrantWhenUserAlreadyHasBadge() {
        givenExistingUser();
        givenExistingBadge();
        givenUserAlreadyHasBadge();

        GrantBadgeResponse result = grantBadge();

        thenBadgeNotGrantedNorSaved(result);
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el usuario no existe")
    void executeShouldThrowNotFoundWhenUserDoesNotExist() {
        givenUnknownUser();

        thenGrantBadgeForUnknownUserThrowsNotFound();
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando el código de insignia no existe")
    void executeShouldThrowNotFoundWhenBadgeCodeDoesNotExist() {
        givenExistingUser();
        givenUnknownBadge();

        thenGrantBadgeWithUnknownBadgeThrowsNotFound();
    }

    // --- arrange ---

    private void givenExistingUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void givenUnknownUser() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
    }

    private void givenExistingBadge() {
        when(badgeRepository.findByCode(BADGE_CODE)).thenReturn(Optional.of(badge));
    }

    private void givenUnknownBadge() {
        when(badgeRepository.findByCode(UNKNOWN_BADGE_CODE)).thenReturn(Optional.empty());
    }

    private void givenUserDoesNotHaveBadge() {
        when(userBadgeRepository.existsByUserIdAndBadgeCode(USER_ID, BADGE_CODE)).thenReturn(false);
    }

    private void givenUserAlreadyHasBadge() {
        when(userBadgeRepository.existsByUserIdAndBadgeCode(USER_ID, BADGE_CODE)).thenReturn(true);
    }

    // --- act ---

    private GrantBadgeResponse grantBadge() {
        return grantBadgeUseCase.execute(new GrantBadgeRequest(EMAIL, BADGE_CODE));
    }

    // --- assert ---

    private void thenBadgeGrantedAndSaved(GrantBadgeResponse result) {
        assertThat(result.granted()).isTrue();
        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadgeRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getBadge()).isEqualTo(badge);
    }

    private void thenBadgeNotGrantedNorSaved(GrantBadgeResponse result) {
        assertThat(result.granted()).isFalse();
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }

    private void thenGrantBadgeForUnknownUserThrowsNotFound() {
        assertThatThrownBy(() -> grantBadgeUseCase.execute(new GrantBadgeRequest(UNKNOWN_EMAIL, BADGE_CODE)))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenGrantBadgeWithUnknownBadgeThrowsNotFound() {
        assertThatThrownBy(() -> grantBadgeUseCase.execute(new GrantBadgeRequest(EMAIL, UNKNOWN_BADGE_CODE)))
                .isInstanceOf(NotFoundException.class);
    }
}
