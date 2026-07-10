package com.huly.backend.infrastructure.presentation.mapper.userGoal;

import com.huly.backend.domain.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.dto.userGoal.AddUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.DeleteUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalRequest;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userGoal.UserGoalPage;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalListResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalPageResponse;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.GoalCompleteResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.UserPlantSummaryResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de metas de usuario:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class UserGoalPresentationMapper {

    // ---- Web -> Domain request ----

    public AcceptChallengeRequest toAcceptChallengeRequest(
            Long userId,
            com.huly.backend.infrastructure.presentation.dto.userGoal.AcceptChallengeRequest request) {
        return new AcceptChallengeRequest(userId, request.title(), request.description(), request.activityId());
    }

    public AddUserGoalRequest toAddUserGoalRequest(
            Long userId,
            com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest request) {
        return new AddUserGoalRequest(userId, request.title(), request.description(), request.activityId());
    }

    public UpdateUserGoalRequest toUpdateUserGoalRequest(
            Long id,
            com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest request) {
        return new UpdateUserGoalRequest(id, request.title(), request.description(), request.activityId());
    }

    public DeleteUserGoalRequest toDeleteUserGoalRequest(Long id) {
        return new DeleteUserGoalRequest(id);
    }

    public GetUserGoalsRequest toGetUserGoalsRequest(Long userId, int page, int size) {
        return new GetUserGoalsRequest(userId, page, size);
    }

    public CompleteUserGoalRequest toCompleteUserGoalRequest(Long id) {
        return new CompleteUserGoalRequest(id);
    }

    // ---- Domain response -> Web ----

    public UserGoalResponse toResponse(AcceptChallengeResponse response) {
        return toResponse(response.goal());
    }

    public UserGoalResponse toResponse(AddUserGoalResponse response) {
        return toResponse(response.goal());
    }

    public UserGoalResponse toResponse(UpdateUserGoalResponse response) {
        return toResponse(response.goal());
    }

    public UserGoalListResponse toListResponse(GetUserGoalsResponse response) {
        return new UserGoalListResponse(
                toPageResponse(response.completados()),
                toPageResponse(response.pendientes())
        );
    }

    public GoalCompleteResponse toGoalCompleteResponse(CompleteUserGoalResponse response) {
        return new GoalCompleteResponse(
                toResponse(response.goal()),
                response.harvestTriggered(),
                response.harvestedPlantNumber(),
                toPlantSummary(response.currentPlant())
        );
    }

    private UserGoalPageResponse toPageResponse(UserGoalPage page) {
        return new UserGoalPageResponse(
                page.content().stream().map(this::toResponse).toList(),
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }

    private UserGoalResponse toResponse(UserGoalItem item) {
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

    private UserPlantSummaryResponse toPlantSummary(UserPlantItem item) {
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
}
