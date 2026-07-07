package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.mapper.user.GetCurrentMembershipMapper;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentMembershipUseCaseTest {

    private static final long USER_ID = 10L;

    @Mock
    private UserPlanRepository userPlanRepository;

    private GetCurrentMembershipUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCurrentMembershipUseCase(userPlanRepository, new GetCurrentMembershipMapper());
    }

    @Test
    @DisplayName("Devuelve la membresía activa cuando está vigente")
    void executeReturnsMembershipWhenActive() {
        givenActiveMembership(USER_ID);

        GetCurrentMembershipResponse result = getMembership(USER_ID);

        thenActivePremium(result);
    }

    @Test
    @DisplayName("Devuelve inactiva cuando la membresía está vencida")
    void executeReturnsInactiveWhenMembershipExpired() {
        givenExpiredMembership(USER_ID);

        GetCurrentMembershipResponse result = getMembership(USER_ID);

        thenInactive(result);
    }

    @Test
    @DisplayName("Devuelve inactiva cuando el usuario no tiene membresía")
    void executeReturnsInactiveWhenUserHasNoMembership() {
        givenNoMembership(USER_ID);

        GetCurrentMembershipResponse result = getMembership(USER_ID);

        thenInactive(result);
    }

    // --- arrange ---

    private void givenActiveMembership(long userId) {
        UserPlan plan = UserPlan.builder()
                .id(1L).userId(userId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(userId)).thenReturn(Optional.of(plan));
    }

    private void givenExpiredMembership(long userId) {
        UserPlan expired = UserPlan.builder()
                .id(1L).userId(userId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(userId)).thenReturn(Optional.of(expired));
    }

    private void givenNoMembership(long userId) {
        when(userPlanRepository.findByUser(userId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private GetCurrentMembershipResponse getMembership(long userId) {
        return useCase.execute(new GetCurrentMembershipRequest(userId));
    }

    // --- assert ---

    private void thenActivePremium(GetCurrentMembershipResponse result) {
        assertThat(result.active()).isTrue();
        assertThat(result.planCode()).isEqualTo("PREMIUM");
    }

    private void thenInactive(GetCurrentMembershipResponse result) {
        assertThat(result.active()).isFalse();
    }
}
