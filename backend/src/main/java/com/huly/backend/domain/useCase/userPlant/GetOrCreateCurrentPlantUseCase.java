package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.model.UserPlant;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class GetOrCreateCurrentPlantUseCase {

    private final UserPlantRepository userPlantRepository;

    @Transactional
    public UserPlant execute(Long userId) {
        UserPlant plant = userPlantRepository.findByUserIdAndStatus(userId, PlantStatus.GROWING)
                .orElseGet(() -> createFirstPlant(userId));
        plant.setCompletedGoalsCount(userPlantRepository.countCompletedGoalsByPlantId(plant.getId()));
        return plant;
    }

    private UserPlant createFirstPlant(Long userId) {
        List<UserPlant> existing = userPlantRepository.findAllByUserIdOrderByPlantNumber(userId);
        int nextNumber = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getPlantNumber() + 1;
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
