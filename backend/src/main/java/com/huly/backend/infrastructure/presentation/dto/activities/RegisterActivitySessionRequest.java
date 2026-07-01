package com.huly.backend.infrastructure.presentation.dto.activities;

import com.huly.backend.domain.model.enums.ActivityType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterActivitySessionRequest {

    @NotNull(message = "El tipo de actividad es obligatorio")
    private ActivityType activityType;

    private String contextId;
}
