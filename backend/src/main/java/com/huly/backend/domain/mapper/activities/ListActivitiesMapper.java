package com.huly.backend.domain.mapper.activities;

import com.huly.backend.domain.dto.activities.ActivityItem;
import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.model.activity.Activity;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de actividades.
 */
public class ListActivitiesMapper {

    public ListActivitiesResponse toResponse(List<Activity> activities) {
        List<ActivityItem> items = activities.stream()
                .map(this::toItem)
                .toList();
        return new ListActivitiesResponse(items);
    }

    private ActivityItem toItem(Activity a) {
        return new ActivityItem(
                a.getId(),
                a.getType(),
                a.getValenceMin(),
                a.getValenceMax(),
                a.getArousalMin(),
                a.getArousalMax(),
                a.getDominanceMin(),
                a.getDominanceMax(),
                a.getEffectValence(),
                a.getEffectArousal(),
                a.getEffectDominance()
        );
    }
}
