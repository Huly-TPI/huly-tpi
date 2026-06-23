package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.user.UserPlan;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserPlanRepository {
    Optional<UserPlan> findByUser(Long userId);
    UserPlan save(UserPlan plan);
    List<UserPlan> findPlansNeedingExpiryReminder(Instant now, Instant threshold);
    void markExpiryReminderSent(Long id, Instant expiresAt);
}
