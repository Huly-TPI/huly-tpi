package com.huly.backend.domain.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.DeleteUserGoalResponse;
import com.huly.backend.domain.model.user.UserGoal;

/**
 * Mapper de dominio para el caso de uso de cancelacion de meta de usuario.
 */
public class DeleteUserGoalMapper {

    public DeleteUserGoalResponse toResponse(UserGoal goal) {
        return new DeleteUserGoalResponse(goal.getId());
    }
}
