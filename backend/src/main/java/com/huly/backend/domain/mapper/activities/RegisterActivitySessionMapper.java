package com.huly.backend.domain.mapper.activities;

import com.huly.backend.domain.dto.activities.RegisterActivitySessionRequest;
import com.huly.backend.domain.dto.activities.RegisterActivitySessionResponse;
import com.huly.backend.domain.model.activity.ActivitySession;

import java.time.Instant;

/**
 * Mapper de dominio para el caso de uso de registro de sesion de actividad.
 */
public class RegisterActivitySessionMapper {

    public ActivitySession toModel(RegisterActivitySessionRequest request) {
        return ActivitySession.builder()
                .userId(request.userId())
                .activityType(request.activityType())
                .createdAt(Instant.now())
                .build();
    }

    public RegisterActivitySessionResponse toResponse(ActivitySession session) {
        return new RegisterActivitySessionResponse(
                session.getId(),
                session.getUserId(),
                session.getActivityType(),
                session.getCreatedAt()
        );
    }
}
