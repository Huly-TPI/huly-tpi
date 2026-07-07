package com.huly.backend.domain.mapper.activities;

import com.huly.backend.domain.dto.admin.activities.UpdateActivityConfigRequest;
import com.huly.backend.domain.model.activity.Activity;

public class UpdateActivityConfigMapper {
    public Activity toModel(Activity existing, UpdateActivityConfigRequest request) {
        return Activity.builder()
                .id(existing.getId())
                .type(existing.getType())
                .valenceMin(request.valenceMin())
                .valenceMax(request.valenceMax())
                .arousalMin(request.arousalMin())
                .arousalMax(request.arousalMax())
                .dominanceMin(request.dominanceMin())
                .dominanceMax(request.dominanceMax())
                .effectValence(request.effectValence())
                .effectArousal(request.effectArousal())
                .effectDominance(request.effectDominance())
                .title(request.title())
                .description(request.description())
                .goalKeywords(request.goalKeywords())
                .routePath(request.routePath())
                .build();
    }
}
