package com.huly.backend.infrastructure.presentation.dto.userGoal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserGoalRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String title,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String description,

        Long activityId
) {}
