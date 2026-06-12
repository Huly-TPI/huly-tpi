package com.huly.backend.domain.useCase.userGoal;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class CompleteUserGoalUseCase {

    private final UserGoalRepository userGoalRepository;
    private final UserPlantRepository userPlantRepository;
    private final GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;

    public record Result(UserGoal goal, boolean harvestTriggered, Integer harvestedPlantNumber, UserPlant currentPlant) {}

    @Transactional
    public Result execute(Long id) {
        UserGoal goal = userGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGoal", "id", id));

        if (goal.getStatus() == GoalStatus.COMPLETED) {
            UserPlant current = getOrCreateCurrentPlantUseCase.execute(goal.getUserId());
            long count = userPlantRepository.countCompletedGoalsByPlantId(current.getId());
            return new Result(goal, false, null, withCount(current, count));
        }

        UserPlant currentPlant = getOrCreateCurrentPlantUseCase.execute(goal.getUserId());

        goal.setStatus(GoalStatus.COMPLETED);
        goal.setUserPlantId(currentPlant.getId());
        UserGoal savedGoal = userGoalRepository.save(goal);

        long completedCount = userPlantRepository.countCompletedGoalsByPlantId(currentPlant.getId());

        if (completedCount >= currentPlant.getRequiredGoals()) {
            currentPlant.setStatus(PlantStatus.COMPLETED);
            currentPlant.setCompletedAt(Instant.now());
            userPlantRepository.save(currentPlant);

            int nextNumber = currentPlant.getPlantNumber() + 1;
            UserPlant nextPlant = userPlantRepository.save(UserPlant.builder()
                    .userId(goal.getUserId())
                    .plantNumber(nextNumber)
                    .requiredGoals(GetOrCreateCurrentPlantUseCase.calculateRequiredGoals(nextNumber))
                    .status(PlantStatus.GROWING)
                    .startedAt(Instant.now())
                    .build());

            return new Result(savedGoal, true, currentPlant.getPlantNumber(), withCount(nextPlant, 0));
        }

        return new Result(savedGoal, false, null, withCount(currentPlant, completedCount));
    }

    private UserPlant withCount(UserPlant plant, long count) {
        plant.setCompletedGoalsCount(count);
        return plant;
    }
}
