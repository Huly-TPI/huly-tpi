package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;

    public void execute(Long id) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));
        goal.setStatus(GoalStatus.CANCELLED);
        userGoalRepository.save(goal);
    }
}
