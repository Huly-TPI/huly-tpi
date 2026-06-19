package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.user.UserGoal;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.useCase.userPlant.GetPlantGoalsUseCase;
import com.huly.backend.domain.useCase.userPlant.GetUserPlantsUseCase;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.PlantGoalsResponse;
import com.huly.backend.infrastructure.presentation.dto.userPlant.UserPlantSummaryResponse;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-plants")
@RequiredArgsConstructor
public class UserPlantController {

    private final GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private final GetUserPlantsUseCase getUserPlantsUseCase;
    private final GetPlantGoalsUseCase getPlantGoalsUseCase;
    private final UserPlantRepository userPlantRepository;

    @GetMapping("/current")
    public ResponseEntity<UserPlantSummaryResponse> getCurrent(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        UserPlant plant = getOrCreateCurrentPlantUseCase.execute(userId);
        return ResponseEntity.ok(toSummary(plant));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserPlantSummaryResponse>> getAll(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        List<UserPlant> plants = getUserPlantsUseCase.execute(userId);
        List<UserPlantSummaryResponse> result = plants.stream().map(p -> {
            long count = userPlantRepository.countCompletedGoalsByPlantId(p.getId());
            p.setCompletedGoalsCount(count);
            return toSummary(p);
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/goals")
    public ResponseEntity<PlantGoalsResponse> getGoals(@PathVariable Long id) {
        List<UserGoal> goals = getPlantGoalsUseCase.execute(id);
        List<UserGoalResponse> goalResponses = goals.stream().map(g -> new UserGoalResponse(
                g.getId(), g.getUserId(), g.getTitle(), g.getDescription(),
                g.getStatus().name(), g.getCreatedAt(), g.getActivityId(),
                g.getImageUrl(), g.getCoinsReward(), g.getCoinsRewardWithImage()
        )).toList();
        return ResponseEntity.ok(new PlantGoalsResponse(id, goalResponses));
    }

    private UserPlantSummaryResponse toSummary(UserPlant plant) {
        return new UserPlantSummaryResponse(
                plant.getId(),
                plant.getPlantNumber(),
                plant.getRequiredGoals(),
                plant.getCompletedGoalsCount() != null ? plant.getCompletedGoalsCount() : 0L,
                plant.getStatus().name(),
                plant.getStartedAt(),
                plant.getCompletedAt()
        );
    }

    private Long getUserId(UserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return Long.parseLong(principal.getUsername());
    }
}
