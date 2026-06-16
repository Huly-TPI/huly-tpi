package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.ActivitySession;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import com.huly.backend.infrastructure.repository.entity.ActivitySessionEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivitySessionJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivitySessionRepositoryImpl implements ActivitySessionRepository {

    private final IActivitySessionJpaRepository activitySessionJpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public ActivitySession save(ActivitySession session) {
        AppUserEntity userEntity = appUserRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ActivitySessionEntity entity = ActivitySessionEntity.builder()
                .user(userEntity)
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
}
