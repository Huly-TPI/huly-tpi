package com.huly.backend.domain.dto.userPlant;

import com.huly.backend.domain.dto.userGoal.UserGoalItem;

import java.util.List;

/**
 * Respuesta de dominio con las metas completadas de una planta.
 */
public record GetPlantGoalsResponse(
        Long plantId,
        List<UserGoalItem> goals
) {
}
