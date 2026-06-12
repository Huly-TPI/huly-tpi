package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.EmotionalEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IEmotionalEventJpaRepository extends JpaRepository<EmotionalEventEntity, Long> {

    @Query("""
            select e
            from EmotionalEventEntity e
            where e.user.id = :userId
              and (e.recommendationDecision is not null or e.feedbackScore is not null)
            order by e.createdAt desc
            """)
    List<EmotionalEventEntity> findRecommendationHistoryByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
