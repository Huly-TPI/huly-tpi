package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.activity.ActivitySession;
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
import static org.mockito.ArgumentMatchers.*;
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
                .activityType(ActivityType.BREATHING)
                .createdAt(now)
                .build();

        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);

        ActivitySessionEntity savedEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.BREATHING)
                .createdAt(now)
                .build();

        when(appUserRepository.getReferenceById(userId)).thenReturn(userEntity);
        when(activitySessionJpaRepository.save(any(ActivitySessionEntity.class))).thenReturn(savedEntity);

        ActivitySession result = activitySessionRepository.save(session);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityType()).isEqualTo(ActivityType.BREATHING);
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
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findByUserId(userId)).thenReturn(java.util.List.of(sessionEntity));

        java.util.List<ActivitySession> result = activitySessionRepository.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getActivityType()).isEqualTo(ActivityType.BREATHING);
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
                .activityType(ActivityType.BREATHING)
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
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, start))
                .thenReturn(List.of(sessionEntity));

        List<ActivitySession> result = activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, start, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(activitySessionJpaRepository).findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, start);
    }

    @Test
    void findByUserIdAndCreatedAtAfter_shouldReturnMappedList() {
        Long userId = 1L;
        Instant start = Instant.now().minusSeconds(3600);
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);
        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(userId, start))
                .thenReturn(List.of(sessionEntity));

        List<ActivitySession> result = activitySessionRepository.findByUserIdAndCreatedAtAfter(userId, start);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(activitySessionJpaRepository).findByUserIdAndCreatedAtAfter(userId, start);
    }

    @Test
    void findRecentByUserId_shouldHandleLimitConditions() {
        Long userId = 1L;
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);
        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        List<ActivitySession> resultZero = activitySessionRepository.findRecentByUserId(userId, 0);
        assertThat(resultZero).isEmpty();

        when(activitySessionJpaRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(sessionEntity));
        List<ActivitySession> resultTen = activitySessionRepository.findRecentByUserId(userId, 10);
        assertThat(resultTen).hasSize(1);
    }

    @Test
    void findRecentByUserIdAndCreatedAtAfter_shouldHandleLimitConditions() {
        Long userId = 1L;
        Instant start = Instant.now().minusSeconds(3600);
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);
        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        List<ActivitySession> resultZero = activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, start, 0);
        assertThat(resultZero).isEmpty();

        when(activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(eq(userId), eq(start), any(Pageable.class)))
                .thenReturn(List.of(sessionEntity));
        List<ActivitySession> resultTen = activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, start, 10);
        assertThat(resultTen).hasSize(1);
    }

    @Test
    void countByUserIdAndCreatedAtAfter_shouldReturnCount() {
        Long userId = 1L;
        Instant start = Instant.now().minusSeconds(3600);
        when(activitySessionJpaRepository.countByUserIdAndCreatedAtAfter(userId, start)).thenReturn(15L);

        long count = activitySessionRepository.countByUserIdAndCreatedAtAfter(userId, start);

        assertThat(count).isEqualTo(15L);
        verify(activitySessionJpaRepository).countByUserIdAndCreatedAtAfter(userId, start);
    }

    @Test
    void findOldestSessionByUserId_shouldReturnOldest() {
        Long userId = 1L;
        AppUserEntity userEntity = new AppUserEntity();
        userEntity.setId(userId);
        ActivitySessionEntity sessionEntity = ActivitySessionEntity.builder()
                .id(10L)
                .user(userEntity)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(activitySessionJpaRepository.findFirstByUserIdOrderByCreatedAtAsc(userId))
                .thenReturn(Optional.of(sessionEntity));

        Optional<ActivitySession> result = activitySessionRepository.findOldestSessionByUserId(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        verify(activitySessionJpaRepository).findFirstByUserIdOrderByCreatedAtAsc(userId);
    }
}

