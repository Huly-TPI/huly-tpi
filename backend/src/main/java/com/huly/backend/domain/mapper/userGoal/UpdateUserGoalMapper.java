package com.huly.backend.domain.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.model.user.UserGoal;

/**
 * Mapper de dominio para el caso de uso de actualizacion de meta de usuario.
 */
public class UpdateUserGoalMapper {

    public UpdateUserGoalResponse toResponse(UserGoal goal) {
        return new UpdateUserGoalResponse(new UserGoalItem(
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
        ));
    }
}
