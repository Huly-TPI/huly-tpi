package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class GetOrCreateCurrentPlantUseCase {

    private final UserPlantRepository userPlantRepository;

    @Transactional
    public UserPlant execute(Long userId) {
        UserPlant plant = userPlantRepository.findLatestByUserIdAndStatus(userId, PlantStatus.GROWING)
                .orElseGet(() -> createNextPlant(userId));
        plant.setCompletedGoalsCount(userPlantRepository.countCompletedGoalsByPlantId(plant.getId()));
        return plant;
    }

    private UserPlant createNextPlant(Long userId) {
        int nextNumber = userPlantRepository.findLatestByUserId(userId)
                .map(UserPlant::getPlantNumber)
                .map(plantNumber -> plantNumber + 1)
                .orElse(1);
        return userPlantRepository.save(UserPlant.builder()
                .userId(userId)
                .plantNumber(nextNumber)
                .requiredGoals(calculateRequiredGoals(nextNumber))
                .status(PlantStatus.GROWING)
                .startedAt(Instant.now())
                .build());
    }

    public static int calculateRequiredGoals(int plantNumber) {
        if (plantNumber <= 5) return 5 + (plantNumber - 1) * 3;
        return ThreadLocalRandom.current().nextInt(10, 26);
    }
}
