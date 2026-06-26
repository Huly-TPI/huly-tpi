package com.huly.backend.domain.dto.activities;

import com.huly.backend.domain.model.enums.ActivityType;

/**
 * Pedido de dominio para registrar una sesion de actividad.
 *
 * @param userId       usuario que realiza la actividad.
 * @param activityType tipo de actividad realizada.
 */
public record RegisterActivitySessionRequest(Long userId, ActivityType activityType) {
}
