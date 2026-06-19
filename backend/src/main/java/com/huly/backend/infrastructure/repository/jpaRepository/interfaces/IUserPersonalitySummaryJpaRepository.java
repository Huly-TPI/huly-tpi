package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserPersonalitySummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserPersonalitySummaryJpaRepository extends JpaRepository<UserPersonalitySummaryEntity, Long> {

    Optional<UserPersonalitySummaryEntity> findByAppUserId(Long userId);

    void deleteByAppUserId(Long userId);
}
