package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserPlanJpaRepository extends JpaRepository<UserPlanEntity, Long> {
    Optional<UserPlanEntity> findByUserId(Long userId);
}
