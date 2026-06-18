package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.ActivitySessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IActivitySessionJpaRepository extends JpaRepository<ActivitySessionEntity, Long> {
    List<ActivitySessionEntity> findByUserId(Long userId);
    List<ActivitySessionEntity> findByUserId(Long userId, Pageable pageable);
    List<ActivitySessionEntity> findByUserIdAndCreatedAtAfter(Long userId, Instant start);
    List<ActivitySessionEntity> findByUserIdAndCreatedAtAfter(Long userId, Instant start, Pageable pageable);
    List<ActivitySessionEntity> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    List<ActivitySessionEntity> findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, Instant start);
    long countByUserIdAndCreatedAtAfter(Long userId, Instant start);
    Optional<ActivitySessionEntity> findFirstByUserIdOrderByCreatedAtAsc(Long userId);
}
