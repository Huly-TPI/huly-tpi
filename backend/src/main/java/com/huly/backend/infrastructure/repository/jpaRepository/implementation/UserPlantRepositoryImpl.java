package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserPlantEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPlantRepositoryImpl implements UserPlantRepository {

    private final IUserPlantJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public UserPlant save(UserPlant userPlant) {
        UserPlantEntity saved = jpaRepository.save(toEntity(userPlant));
        return toDomain(saved);
    }

    @Override
    public Optional<UserPlant> findByUserIdAndStatus(Long userId, PlantStatus status) {
        return jpaRepository.findByAppUser_IdAndStatus(userId, status).map(this::toDomain);
    }

    @Override
    public List<UserPlant> findAllByUserIdOrderByPlantNumber(Long userId) {
        return jpaRepository.findByAppUser_IdOrderByPlantNumberAsc(userId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countCompletedGoalsByPlantId(Long plantId) {
        return jpaRepository.countByUserPlantIdAndStatus(plantId, GoalStatus.COMPLETED);
    }

    private UserPlantEntity toEntity(UserPlant domain) {
        AppUserEntity appUser = appUserRepository.getReferenceById(domain.getUserId());
        return UserPlantEntity.builder()
                .id(domain.getId())
                .appUser(appUser)
                .plantNumber(domain.getPlantNumber())
                .requiredGoals(domain.getRequiredGoals())
                .status(domain.getStatus())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .build();
    }

    private UserPlant toDomain(UserPlantEntity entity) {
        return UserPlant.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .plantNumber(entity.getPlantNumber())
                .requiredGoals(entity.getRequiredGoals())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
