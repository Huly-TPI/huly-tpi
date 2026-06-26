package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.dto.userGoal.UpdateUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.userGoal.UpdateUserGoalMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;
    private final UpdateUserGoalMapper mapper;

    public UpdateUserGoalResponse execute(UpdateUserGoalRequest request) {
        UserGoal existing = userGoalRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", request.id()));
        existing.setTitle(request.title());
        existing.setDescription(request.description());
        existing.setActivityId(request.activityId());
        return mapper.toResponse(userGoalRepository.save(existing));
    }
}
