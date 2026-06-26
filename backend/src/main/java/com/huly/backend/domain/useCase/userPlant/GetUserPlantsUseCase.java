package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetUserPlantsRequest;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.mapper.userPlant.GetUserPlantsMapper;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetUserPlantsUseCase {

    private final UserPlantRepository userPlantRepository;
    private final GetUserPlantsMapper mapper;

    public GetUserPlantsResponse execute(GetUserPlantsRequest request) {
        List<UserPlant> plants = userPlantRepository.findAllByUserIdOrderByPlantNumber(request.userId());
        plants.forEach(plant ->
                plant.setCompletedGoalsCount(userPlantRepository.countCompletedGoalsByPlantId(plant.getId())));
        return mapper.toResponse(plants);
    }
}
