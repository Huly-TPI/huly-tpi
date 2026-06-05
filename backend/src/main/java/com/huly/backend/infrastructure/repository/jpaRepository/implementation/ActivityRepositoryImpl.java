package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.repository.ActivityRepository;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ActivityRepositoryImpl implements ActivityRepository {
    
    private final IActivityJpaRepository activityJpaRepository;

     @Override
    public List<Activity> findAll() {
        return activityJpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .toList();
                }

    @Override
    public boolean existsById(Long id) {
        return activityJpaRepository.existsById(id);
    }

    private Activity mapToDomain(ActivityEntity entity) {
        return Activity.builder()
                .id(entity.getId())
                .type(entity.getType())
                .valenceMin(entity.getValenceMin())
                .valenceMax(entity.getValenceMax())
                .arousalMin(entity.getArousalMin())
                .arousalMax(entity.getArousalMax())
                .dominanceMin(entity.getDominanceMin())
                .dominanceMax(entity.getDominanceMax())
                .effectValence(entity.getEffectValence())
                .effectArousal(entity.getEffectArousal())
                .effectDominance(entity.getEffectDominance())
                .build(
        );
    }

    
}
