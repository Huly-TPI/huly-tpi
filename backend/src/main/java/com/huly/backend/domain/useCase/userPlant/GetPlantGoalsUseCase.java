package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetPlantGoalsRequest;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.mapper.userPlant.GetPlantGoalsMapper;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.repository.user.UserGoalRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetPlantGoalsUseCase {

    private final UserGoalRepository userGoalRepository;
    private final GetPlantGoalsMapper mapper;

    public GetPlantGoalsResponse execute(GetPlantGoalsRequest request) {
        List<UserGoal> goals = userGoalRepository.findCompletedByPlantId(request.plantId());
        return mapper.toResponse(request.plantId(), goals);
    }
}
