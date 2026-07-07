package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PendingDailyRecommendationTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IPendingDailyRecommendationTaskJpaRepository extends JpaRepository<PendingDailyRecommendationTaskEntity, Long> {
    List<PendingDailyRecommendationTaskEntity> findAllByRecommendation_Id(Long recommendationId);
    void deleteAllByRecommendation_Id(Long recommendationId);

    List<PendingDailyRecommendationTaskEntity> findAllByRecommendation_User_IdAndRecommendation_RecommendationDate(
            Long userId, LocalDate recommendationDate);
}
