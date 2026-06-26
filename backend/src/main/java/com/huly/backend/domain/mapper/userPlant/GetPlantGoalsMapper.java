package com.huly.backend.domain.mapper.userPlant;

import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.model.user.UserGoal;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de metas completadas de una planta.
 */
public class GetPlantGoalsMapper {

    public GetPlantGoalsResponse toResponse(Long plantId, List<UserGoal> goals) {
        List<UserGoalItem> items = goals.stream()
                .map(this::toItem)
                .toList();
        return new GetPlantGoalsResponse(plantId, items);
    }

    private UserGoalItem toItem(UserGoal goal) {
        return new UserGoalItem(
                goal.getId(),
                goal.getUserId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus().name(),
                goal.getCreatedAt(),
                goal.getActivityId(),
                goal.getImageUrl(),
                goal.getCoinsReward(),
                goal.getCoinsRewardWithImage()
        );
    }
}
