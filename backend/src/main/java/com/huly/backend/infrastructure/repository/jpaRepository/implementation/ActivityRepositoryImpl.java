package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ActivityMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ActivityRepositoryImpl implements ActivityRepository {
    
    private final IActivityJpaRepository activityJpaRepository;
    private final ActivityMapper activityMapper;

     @Override
    public List<Activity> findAll() {
        return activityJpaRepository.findAll()
                .stream()
                .map(activityMapper::toDomain)
                .toList();
                }

    @Override
    public boolean existsById(Long id) {
        return activityJpaRepository.existsById(id);
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return activityJpaRepository.findById(id).map(activityMapper::toDomain);
    }

    @Override
    public Activity save(Activity activity) {
        ActivityEntity entity = activityMapper.toEntity(activity);
        ActivityEntity saved = activityJpaRepository.save(entity);
        return activityMapper.toDomain(saved);
    }
}
