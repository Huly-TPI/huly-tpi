package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GetUserGoalsByUserUseCase {

    private final UserGoalRepository userGoalRepository;

    public Page<UserGoal> executeCompleted(Long userId, Pageable pageable) {
        return userGoalRepository.findByUserIdAndStatus(userId, GoalStatus.COMPLETED, pageable);
    }

    public Page<UserGoal> executePending(Long userId, Pageable pageable) {
        return userGoalRepository.findByUserIdAndStatus(userId, GoalStatus.PENDING, pageable);
    }
}