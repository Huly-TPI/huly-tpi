package com.huly.backend.domain.repository.activity;

import com.huly.backend.domain.model.activity.ActivitySession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ActivitySessionRepository {
    ActivitySession save(ActivitySession session);
    List<ActivitySession> findByUserId(Long userId);
    List<ActivitySession> findByUserIdAndCreatedAtAfter(Long userId, Instant start);
    List<ActivitySession> findRecentByUserId(Long userId, int limit);
    List<ActivitySession> findRecentByUserIdAndCreatedAtAfter(Long userId, Instant start, int limit);
    long countByUserIdAndCreatedAtAfter(Long userId, Instant start);
    Optional<ActivitySession> findOldestSessionByUserId(Long userId);
}
