package com.huly.backend.infrastructure.presentation.mapper.userPlant;

import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantRequest;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsRequest;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsRequest;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.PlantGoalsResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.UserPlantSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de plantas de usuario:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class UserPlantPresentationMapper {

    // ---- Web -> Domain request ----

    public GetCurrentPlantRequest toGetCurrentPlantRequest(Long userId) {
        return new GetCurrentPlantRequest(userId);
    }

    public GetUserPlantsRequest toGetUserPlantsRequest(Long userId) {
        return new GetUserPlantsRequest(userId);
    }

    public GetPlantGoalsRequest toGetPlantGoalsRequest(Long plantId) {
        return new GetPlantGoalsRequest(plantId);
    }

    // ---- Domain response -> Web ----

    public UserPlantSummaryResponse toSummary(GetCurrentPlantResponse response) {
        return toSummary(response.plant());
    }

    public List<UserPlantSummaryResponse> toSummaries(GetUserPlantsResponse response) {
        return response.plants().stream()
                .map(this::toSummary)
                .toList();
    }

    public PlantGoalsResponse toPlantGoalsResponse(GetPlantGoalsResponse response) {
        List<UserGoalResponse> goals = response.goals().stream()
                .map(this::toGoalResponse)
                .toList();
        return new PlantGoalsResponse(response.plantId(), goals);
    }

    private UserPlantSummaryResponse toSummary(UserPlantItem item) {
        return new UserPlantSummaryResponse(
                item.id(),
                item.plantNumber(),
                item.requiredGoals(),
                item.completedGoalsCount() != null ? item.completedGoalsCount() : 0L,
                item.status(),
                item.startedAt(),
                item.completedAt()
        );
    }

    private UserGoalResponse toGoalResponse(UserGoalItem item) {
        return new UserGoalResponse(
                item.id(),
                item.userId(),
                item.title(),
                item.description(),
                item.status(),
                item.createdAt(),
                item.activityId(),
                item.imageUrl(),
                item.coinsReward(),
                item.coinsRewardWithImage()
        );
    }
}
