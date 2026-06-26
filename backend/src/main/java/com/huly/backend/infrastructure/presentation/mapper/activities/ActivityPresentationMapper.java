package com.huly.backend.infrastructure.presentation.mapper.activities;

import com.huly.backend.domain.dto.activities.ActivityItem;
import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.presentation.dto.activities.ActivityResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de actividades:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class ActivityPresentationMapper {

    public RegisterActivitySessionRequest toRegisterRequest(Long userId, ActivityType activityType) {
        return new RegisterActivitySessionRequest(userId, activityType);
    }

    public List<ActivityResponse> toActivityResponses(ListActivitiesResponse response) {
        return response.activities().stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private ActivityResponse toActivityResponse(ActivityItem item) {
        return new ActivityResponse(
                item.id(),
                item.type(),
                item.valenceMin(),
                item.valenceMax(),
                item.arousalMin(),
                item.arousalMax(),
                item.dominanceMin(),
                item.dominanceMax(),
                item.effectValence(),
                item.effectArousal(),
                item.effectDominance()
        );
    }
}
