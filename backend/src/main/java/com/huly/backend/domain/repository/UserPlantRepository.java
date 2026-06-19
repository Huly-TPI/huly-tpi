package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.PlantStatus;

import java.util.List;
import java.util.Optional;

public interface UserPlantRepository {
    UserPlant save(UserPlant userPlant);
    Optional<UserPlant> findByUserIdAndStatus(Long userId, PlantStatus status);
    List<UserPlant> findAllByUserIdOrderByPlantNumber(Long userId);
    long countCompletedGoalsByPlantId(Long plantId);
}
