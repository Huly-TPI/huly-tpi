package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetPlantGoalsUseCase {

    private final UserGoalRepository userGoalRepository;

    public List<UserGoal> execute(Long plantId) {
        return userGoalRepository.findCompletedByPlantId(plantId);
    }
}
