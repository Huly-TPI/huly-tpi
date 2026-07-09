package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface IPendingDailyRecommendationJpaRepository extends JpaRepository<PendingDailyRecommendationEntity, Long> {
    Optional<PendingDailyRecommendationEntity> findByUser_IdAndRecommendationDate(Long userId, LocalDate recommendationDate);
    Optional<PendingDailyRecommendationEntity> findByIdAndUser_Id(Long id, Long userId);
}
