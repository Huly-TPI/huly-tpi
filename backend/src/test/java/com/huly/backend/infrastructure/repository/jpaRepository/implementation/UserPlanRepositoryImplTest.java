package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.dto.payment.UserPlan;
import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlanJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPlanRepositoryImplTest {

    @Mock private IUserPlanJpaRepository jpaRepository;
    @InjectMocks private UserPlanRepositoryImpl repository;

    private UserPlanEntity entity() {
        return UserPlanEntity.builder()
                .id(1L).userId(10L).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void findByUser_shouldReturnMappedPlan_whenFound() {
        UserPlanEntity entity = entity();
        when(jpaRepository.findByUserId(10L)).thenReturn(Optional.of(entity));

        Optional<UserPlan> result = repository.findByUser(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(10L);
        assertThat(result.get().getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get().getGrantedAt()).isEqualTo(entity.getGrantedAt());
        assertThat(result.get().getExpiresAt()).isEqualTo(entity.getExpiresAt());
    }

    @Test
    void findByUser_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThat(repository.findByUser(99L)).isEmpty();
    }

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        Instant granted = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant expires = Instant.now().plus(28, ChronoUnit.DAYS);
        UserPlan domain = UserPlan.builder()
                .id(1L).userId(10L).planCode("PRO")
                .grantedAt(granted).expiresAt(expires)
                .build();
        when(jpaRepository.save(any(UserPlanEntity.class))).thenReturn(
                UserPlanEntity.builder()
                        .id(1L).userId(10L).planCode("PRO")
                        .grantedAt(granted).expiresAt(expires)
                        .build());

        UserPlan result = repository.save(domain);

        ArgumentCaptor<UserPlanEntity> captor = ArgumentCaptor.forClass(UserPlanEntity.class);
        verify(jpaRepository).save(captor.capture());
        UserPlanEntity persisted = captor.getValue();
        assertThat(persisted.getId()).isEqualTo(1L);
        assertThat(persisted.getUserId()).isEqualTo(10L);
        assertThat(persisted.getPlanCode()).isEqualTo("PRO");
        assertThat(persisted.getGrantedAt()).isEqualTo(granted);
        assertThat(persisted.getExpiresAt()).isEqualTo(expires);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPlanCode()).isEqualTo("PRO");
    }
}
