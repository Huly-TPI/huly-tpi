package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.mapper.user.GetCurrentMembershipMapper;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock private UserPlanRepository userPlanRepository;
    private GetCurrentMembershipUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCurrentMembershipUseCase(userPlanRepository, new GetCurrentMembershipMapper());
    }

    @Test
    void execute_shouldReturnMembership_whenActive() {
        UserPlan plan = UserPlan.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(plan));

        GetCurrentMembershipResponse result = useCase.execute(new GetCurrentMembershipRequest(10L));

        assertThat(result.active()).isTrue();
        assertThat(result.planCode()).isEqualTo("PREMIUM");
    }

    @Test
    void execute_shouldReturnInactive_whenMembershipExpired() {
        UserPlan expired = UserPlan.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(expired));

        GetCurrentMembershipResponse result = useCase.execute(new GetCurrentMembershipRequest(10L));

        assertThat(result.active()).isFalse();
    }

    @Test
    void execute_shouldReturnInactive_whenUserHasNoMembership() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.empty());

        GetCurrentMembershipResponse result = useCase.execute(new GetCurrentMembershipRequest(10L));

        assertThat(result.active()).isFalse();
    }
}
