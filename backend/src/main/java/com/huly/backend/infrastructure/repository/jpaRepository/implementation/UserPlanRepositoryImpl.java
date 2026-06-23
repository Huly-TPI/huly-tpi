package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPlanRepositoryImpl implements UserPlanRepository {

    private final IUserPlanJpaRepository jpaRepository;

    @Override
    public Optional<UserPlan> findByUser(Long userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public UserPlan save(UserPlan plan) {
        return toDomain(jpaRepository.save(toEntity(plan)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPlan> findPlansNeedingExpiryReminder(Instant now, Instant threshold) {
        return jpaRepository.findPlansNeedingExpiryReminder(now, threshold).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markExpiryReminderSent(Long id, Instant expiresAt) {
        jpaRepository.markExpiryReminderSent(id, expiresAt);
    }

    private UserPlan toDomain(UserPlanEntity e) {
        return UserPlan.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .productId(e.getProductId())
                .planCode(e.getPlanCode())
                .grantedAt(e.getGrantedAt())
                .expiresAt(e.getExpiresAt())
                .expiryReminderSentFor(e.getExpiryReminderSentFor())
                .build();
    }

    private UserPlanEntity toEntity(UserPlan p) {
        return UserPlanEntity.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .productId(p.getProductId())
                .planCode(p.getPlanCode())
                .grantedAt(p.getGrantedAt())
                .expiresAt(p.getExpiresAt())
                .expiryReminderSentFor(p.getExpiryReminderSentFor())
                .build();
    }
}
