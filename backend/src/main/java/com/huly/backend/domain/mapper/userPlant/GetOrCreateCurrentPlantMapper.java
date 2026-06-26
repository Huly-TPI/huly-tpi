package com.huly.backend.domain.mapper.userPlant;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.model.user.UserPlant;

/**
 * Mapper de dominio para el caso de uso de obtener/crear la planta actual.
 */
public class GetOrCreateCurrentPlantMapper {

    public GetCurrentPlantResponse toResponse(UserPlant plant) {
        return new GetCurrentPlantResponse(toItem(plant));
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
