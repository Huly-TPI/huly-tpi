package com.huly.backend.domain.dto.userGoal;

import com.huly.backend.domain.dto.userPlant.UserPlantItem;

/**
 * Respuesta de dominio luego de completar una meta de usuario.
 *
 * @param goal                  meta resultante.
 * @param harvestTriggered      indica si se completo la planta actual (cosecha).
 * @param harvestedPlantNumber  numero de la planta cosechada (null si no hubo cosecha).
 * @param currentPlant          planta actual del usuario tras la operacion.
 */
public record CompleteUserGoalResponse(
        UserGoalItem goal,
        boolean harvestTriggered,
        Integer harvestedPlantNumber,
        UserPlantItem currentPlant
) {
}
