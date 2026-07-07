package com.huly.backend.domain.repository.pending;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.model.pending.PendingDailyRecommendation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface PendingRecommendationRepository {

    Optional<PendingDailyRecommendation> findByUserIdAndDate(Long userId, LocalDate date);

    Optional<PendingDailyRecommendation> findByIdAndUserId(Long id, Long userId);

    PendingDailyRecommendation upsert(PendingDailyRecommendation recommendation);

    PendingDailyRecommendation updateDecision(Long recommendationId, RecommendationResponseDecision decision, Instant decidedAt);

    java.util.Set<Long> findAcceptedTaskIds(Long userId, LocalDate date);
}
