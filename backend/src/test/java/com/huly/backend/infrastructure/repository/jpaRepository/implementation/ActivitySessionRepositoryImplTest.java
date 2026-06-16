package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivitySessionEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivitySessionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitySessionRepositoryImplTest {

    @Mock
    private IActivitySessionJpaRepository activitySessionJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ActivitySessionRepositoryImpl activitySessionRepository;

    @Test
    void save_shouldSaveAndReturnDomainModel() {
        Long userId = 1L;
        Instant now = Instant.now();
        ActivitySession session = ActivitySession.builder()
                .userId(userId)
                .activityType(ActivityType.RESPIRACION)
                .createdAt(now)
                .build();

        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);

        ActivitySessionEntity savedEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.RESPIRACION)
                .createdAt(now)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(activitySessionJpaRepository.save(any(ActivitySessionEntity.class))).thenReturn(savedEntity);

        ActivitySession result = activitySessionRepository.save(session);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityType()).isEqualTo(ActivityType.RESPIRACION);
        verify(appUserRepository).findById(userId);
        verify(activitySessionJpaRepository).save(any(ActivitySessionEntity.class));
    }
}
