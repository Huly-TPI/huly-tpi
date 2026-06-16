package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;

    public UserGoal execute(Long id, String title, String description, Long activityId) {
        UserGoal existing = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));
        existing.setTitle(title);
        existing.setDescription(description);
        existing.setActivityId(activityId);
        return userGoalRepository.save(existing);
    }
}
