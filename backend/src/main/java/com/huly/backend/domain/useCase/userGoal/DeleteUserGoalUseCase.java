package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;

    public void execute(Long id) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("UserGoal", "id", id));
        goal.setStatus(GoalStatus.CANCELLED);
        userGoalRepository.save(goal);
    }
}
