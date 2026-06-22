package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.PlantStatus;

import java.util.List;
import java.util.Optional;

public interface UserPlantRepository {
    UserPlant save(UserPlant userPlant);
    UserPlant saveAndFlush(UserPlant userPlant);
    Optional<UserPlant> findLatestByUserIdAndStatus(Long userId, PlantStatus status);
    Optional<UserPlant> findLatestByUserId(Long userId);
    List<UserPlant> findAllByUserIdOrderByPlantNumber(Long userId);
    long countCompletedGoalsByPlantId(Long plantId);
}
