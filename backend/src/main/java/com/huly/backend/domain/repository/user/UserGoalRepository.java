package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserGoalRepository {
    UserGoal save(UserGoal userGoal);
    Page<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status, Pageable pageable);
    Optional<UserGoal> findById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
}
