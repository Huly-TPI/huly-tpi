package com.huly.backend.domain.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.AddUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.user.UserGoal;

import java.time.Instant;

/**
 * Mapper de dominio para el caso de uso de creacion de meta de usuario.
 */
public class AddUserGoalMapper {

    public UserGoal toModel(AddUserGoalRequest request) {
        return UserGoal.builder()
                .userId(request.userId())
                .title(request.title())
                .description(request.description())
                .activityId(request.activityId())
                .status(GoalStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public AddUserGoalResponse toResponse(UserGoal goal) {
        return new AddUserGoalResponse(new UserGoalItem(
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
