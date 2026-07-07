package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public Activity toDomain(ActivityEntity entity) {
        if (entity == null) {
            return null;
        }
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
                .title(entity.getTitle())
                .description(entity.getDescription())
                .goalKeywords(entity.getGoalKeywords())
                .routePath(entity.getRoutePath())
                .build();
    }

    public ActivityEntity toEntity(Activity activity) {
        if (activity == null) {
            return null;
        }
        return ActivityEntity.builder()
                .id(activity.getId())
                .type(activity.getType())
                .valenceMin(activity.getValenceMin())
                .valenceMax(activity.getValenceMax())
                .arousalMin(activity.getArousalMin())
                .arousalMax(activity.getArousalMax())
                .dominanceMin(activity.getDominanceMin())
                .dominanceMax(activity.getDominanceMax())
                .effectValence(activity.getEffectValence())
                .effectArousal(activity.getEffectArousal())
                .effectDominance(activity.getEffectDominance())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .goalKeywords(activity.getGoalKeywords())
                .routePath(activity.getRoutePath())
                .build();
    }
}
