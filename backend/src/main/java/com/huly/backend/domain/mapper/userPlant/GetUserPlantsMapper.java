package com.huly.backend.domain.mapper.userPlant;

import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.model.user.UserPlant;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de plantas de usuario.
 */
public class GetUserPlantsMapper {

    public GetUserPlantsResponse toResponse(List<UserPlant> plants) {
        List<UserPlantItem> items = plants.stream()
                .map(this::toItem)
                .toList();
        return new GetUserPlantsResponse(items);
    }

    private UserPlantItem toItem(UserPlant plant) {
        return new UserPlantItem(
                plant.getId(),
                plant.getPlantNumber(),
                plant.getRequiredGoals(),
                plant.getCompletedGoalsCount() != null ? plant.getCompletedGoalsCount() : 0L,
                plant.getStatus().name(),
                plant.getStartedAt(),
                plant.getCompletedAt()
        );
    }
}
