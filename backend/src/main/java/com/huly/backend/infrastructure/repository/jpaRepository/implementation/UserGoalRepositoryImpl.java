package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.repository.UserGoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserGoalsEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserGoalJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGoalRepositoryImpl implements UserGoalRepository {

    private final IUserGoalJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;
    private final IActivityJpaRepository activityJpaRepository;

    @Override
    public UserGoal save(UserGoal userGoal) {
        UserGoalsEntity saved = jpaRepository.save(toEntity(userGoal));
        return toDomain(saved);
    }

    @Override
    public Page<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status, Pageable pageable) {
        return jpaRepository.findByAppUser_IdAndStatus(userId, status, pageable)
                .map(this::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<UserGoal> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private UserGoalsEntity toEntity(UserGoal domain) {
        AppUserEntity appUser = appUserRepository.getReferenceById(domain.getUserId());

        ActivityEntity activity = domain.getActivityId() != null
                ? activityJpaRepository.getReferenceById(domain.getActivityId())
                : null;

        return UserGoalsEntity.builder()
                .id(domain.getId())
                .appUser(appUser)
                .title(domain.getTitle())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .activity(activity)
                .build();
    }

    private UserGoal toDomain(UserGoalsEntity entity) {
        return UserGoal.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .activityId(entity.getActivity() != null ? entity.getActivity().getId() : null)
                .build();
    }
}
