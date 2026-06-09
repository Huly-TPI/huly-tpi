package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;

    @Transactional
    public UserGoal execute(Long id) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            return goal;
        }
        goal.setStatus(GoalStatus.COMPLETED);
        return userGoalRepository.save(goal);
    }
}
