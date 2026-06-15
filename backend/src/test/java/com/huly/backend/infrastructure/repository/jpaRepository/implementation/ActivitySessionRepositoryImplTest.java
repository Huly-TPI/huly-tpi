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
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
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

        when(appUserRepository.getReferenceById(userId)).thenReturn(userEntity);
        when(activitySessionJpaRepository.save(any(ActivitySessionEntity.class))).thenReturn(savedEntity);

        ActivitySession result = activitySessionRepository.save(session);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityType()).isEqualTo(ActivityType.RESPIRACION);
        verify(appUserRepository).getReferenceById(userId);
        verify(activitySessionJpaRepository).save(any(ActivitySessionEntity.class));
    }

    @Test
    void findByUserId_shouldReturnMappedList() {
        Long userId = 1L;
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);

        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.RESPIRACION)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findByUserId(userId)).thenReturn(java.util.List.of(sessionEntity));

        java.util.List<ActivitySession> result = activitySessionRepository.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getActivityType()).isEqualTo(ActivityType.RESPIRACION);
        verify(activitySessionJpaRepository).findByUserId(userId);
    }

    @Test
    void findRecentByUserId_shouldReturnMappedList() {
        Long userId = 1L;
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);

        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.RESPIRACION)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(sessionEntity));

        List<ActivitySession> result = activitySessionRepository.findRecentByUserId(userId, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(activitySessionJpaRepository).findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void findRecentByUserIdAndCreatedAtAfter_shouldReturnMappedList() {
        Long userId = 1L;
        Instant start = Instant.now().minusSeconds(3600);
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);

        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.RESPIRACION)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, start))
                .thenReturn(List.of(sessionEntity));

        List<ActivitySession> result = activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, start, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(activitySessionJpaRepository).findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, start);
    }
}
