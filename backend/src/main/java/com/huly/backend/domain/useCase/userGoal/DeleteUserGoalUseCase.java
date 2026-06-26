package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.DeleteUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.DeleteUserGoalResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.userGoal.DeleteUserGoalMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;
    private final DeleteUserGoalMapper mapper;

    public DeleteUserGoalResponse execute(DeleteUserGoalRequest request) {
        UserGoal goal = userGoalRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", request.id()));
        goal.setStatus(GoalStatus.CANCELLED);
        UserGoal saved = userGoalRepository.save(goal);
        return mapper.toResponse(saved);
    }
}
