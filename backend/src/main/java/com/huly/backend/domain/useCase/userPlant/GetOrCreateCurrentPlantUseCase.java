package com.huly.backend.domain.useCase.userPlant;

import com.huly.backend.domain.dto.userPlant.GetCurrentPlantRequest;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.mapper.userPlant.GetOrCreateCurrentPlantMapper;
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
    private final GetOrCreateCurrentPlantMapper mapper;

    @Transactional
    public GetCurrentPlantResponse execute(GetCurrentPlantRequest request) {
        return mapper.toResponse(resolveCurrentPlant(request.userId()));
    }

    /**
     * Resuelve la planta actual del usuario devolviendo el modelo de dominio vivo.
     * Lo usa internamente {@code CompleteUserGoalUseCase}, que necesita mutar la entidad.
     */
    @Transactional
    public UserPlant resolveCurrentPlant(Long userId) {
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
