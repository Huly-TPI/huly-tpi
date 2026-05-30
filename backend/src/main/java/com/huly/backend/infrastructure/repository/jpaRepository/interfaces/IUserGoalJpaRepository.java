package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.infrastructure.repository.entity.UserGoalsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserGoalJpaRepository extends JpaRepository<UserGoalsEntity, Long> {
    Page<UserGoalsEntity> findByAppUser_IdAndStatus(Long userId, GoalStatus status, Pageable pageable);
}
