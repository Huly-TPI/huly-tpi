package com.huly.backend.domain.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userGoal.UserGoalPage;
import com.huly.backend.domain.model.user.UserGoal;
import org.springframework.data.domain.Page;

/**
 * Mapper de dominio para el caso de uso de listado paginado de metas de usuario.
 * Traduce las paginas de Spring Data a paginas de dominio sin exponer tipos de Spring.
 */
public class GetUserGoalsMapper {

    public GetUserGoalsResponse toResponse(Page<UserGoal> completados, Page<UserGoal> pendientes) {
        return new GetUserGoalsResponse(toPage(completados), toPage(pendientes));
    }

    private UserGoalPage toPage(Page<UserGoal> page) {
        return new UserGoalPage(
                page.getContent().stream().map(this::toItem).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
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
}
