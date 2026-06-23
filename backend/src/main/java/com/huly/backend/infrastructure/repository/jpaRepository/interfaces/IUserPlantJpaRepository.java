package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.infrastructure.repository.entity.UserGoalsEntity;
import com.huly.backend.infrastructure.repository.entity.UserPlantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserPlantJpaRepository extends JpaRepository<UserPlantEntity, Long> {

    Optional<UserPlantEntity> findTopByAppUser_IdAndStatusOrderByPlantNumberDescStartedAtDescIdDesc(Long userId, PlantStatus status);

    Optional<UserPlantEntity> findTopByAppUser_IdOrderByPlantNumberDescStartedAtDescIdDesc(Long userId);

    List<UserPlantEntity> findByAppUser_IdOrderByPlantNumberAsc(Long userId);

    @Query("SELECT COUNT(g) FROM UserGoalsEntity g WHERE g.userPlant.id = :plantId AND g.status = :status")
    long countByUserPlantIdAndStatus(@Param("plantId") Long plantId, @Param("status") GoalStatus status);

    @Query("SELECT g FROM UserGoalsEntity g WHERE g.userPlant.id = :plantId AND g.status = :status")
    List<UserGoalsEntity> findGoalsByUserPlantIdAndStatus(@Param("plantId") Long plantId, @Param("status") GoalStatus status);
}
