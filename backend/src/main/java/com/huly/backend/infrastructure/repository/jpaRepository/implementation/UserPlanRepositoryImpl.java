package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    private UserPlan toDomain(UserPlanEntity e) {
        return UserPlan.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .productId(e.getProductId())
                .planCode(e.getPlanCode())
                .grantedAt(e.getGrantedAt())
                .expiresAt(e.getExpiresAt())
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
                .build();
    }
}
