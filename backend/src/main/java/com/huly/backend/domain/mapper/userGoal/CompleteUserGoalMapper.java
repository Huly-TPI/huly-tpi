package com.huly.backend.domain.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.user.UserPlant;

/**
 * Mapper de dominio para el caso de uso de completar una meta de usuario.
 */
public class CompleteUserGoalMapper {

    public CompleteUserGoalResponse toResponse(UserGoal goal,
                                               boolean harvestTriggered,
                                               Integer harvestedPlantNumber,
                                               UserPlant currentPlant) {
        return new CompleteUserGoalResponse(
                toItem(goal),
                harvestTriggered,
                harvestedPlantNumber,
                toPlantItem(currentPlant)
        );
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

    private UserPlantItem toPlantItem(UserPlant plant) {
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
