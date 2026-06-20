package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.user.UserPlan;

import java.util.Optional;

public interface UserPlanRepository {
    Optional<UserPlan> findByUser(Long userId);
    UserPlan save(UserPlan plan);
}
