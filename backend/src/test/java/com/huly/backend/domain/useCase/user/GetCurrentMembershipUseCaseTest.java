package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.domain.repository.UserPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    @InjectMocks private GetCurrentMembershipUseCase useCase;

    @Test
    void execute_shouldReturnMembership_whenActive() {
        UserPlan plan = UserPlan.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(plan));

        Optional<UserPlan> result = useCase.execute(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get().getUserId()).isEqualTo(10L);
    }

    @Test
    void execute_shouldReturnEmpty_whenMembershipExpired() {
        UserPlan expired = UserPlan.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.of(expired));

        Optional<UserPlan> result = useCase.execute(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldReturnEmpty_whenUserHasNoMembership() {
        when(userPlanRepository.findByUser(10L)).thenReturn(Optional.empty());

        Optional<UserPlan> result = useCase.execute(10L);

        assertThat(result).isEmpty();
    }
}
