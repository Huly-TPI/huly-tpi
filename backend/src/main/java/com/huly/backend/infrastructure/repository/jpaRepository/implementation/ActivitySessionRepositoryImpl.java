package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.infrastructure.repository.entity.ActivitySessionEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivitySessionJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ActivitySessionRepositoryImpl implements ActivitySessionRepository {

    private final IActivitySessionJpaRepository activitySessionJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public ActivitySession save(ActivitySession session) {
        AppUserEntity user = appUserRepository.getReferenceById(session.getUserId());
        ActivitySessionEntity entity = ActivitySessionEntity.builder()
                .id(session.getId())
                .user(user)
                .activityType(session.getActivityType())
                .createdAt(session.getCreatedAt())
                .build();
        ActivitySessionEntity saved = activitySessionJpaRepository.save(entity);
        return ActivitySession.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .activityType(saved.getActivityType())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public List<ActivitySession> findByUserId(Long userId) {
        return activitySessionJpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ActivitySession> findByUserIdAndCreatedAtAfter(Long userId, Instant start) {
        return activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(userId, start).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ActivitySession> findRecentByUserId(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        if (limit == 5) {
            return activitySessionJpaRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(this::toDomain)
                    .toList();
        }
        return activitySessionJpaRepository.findByUserId(
                        userId,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ActivitySession> findRecentByUserIdAndCreatedAtAfter(Long userId, Instant start, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        if (limit == 5) {
            return activitySessionJpaRepository.findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, start).stream()
                    .map(this::toDomain)
                    .toList();
        }
        return activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(
                        userId,
                        start,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndCreatedAtAfter(Long userId, Instant start) {
        return activitySessionJpaRepository.countByUserIdAndCreatedAtAfter(userId, start);
    }

    @Override
    public Optional<ActivitySession> findOldestSessionByUserId(Long userId) {
        return activitySessionJpaRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
                .map(this::toDomain);
    }

    private ActivitySession toDomain(ActivitySessionEntity saved) {
        return ActivitySession.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .activityType(saved.getActivityType())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
