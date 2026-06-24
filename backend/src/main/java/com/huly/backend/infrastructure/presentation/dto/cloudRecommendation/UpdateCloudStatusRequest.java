package com.huly.backend.infrastructure.presentation.dto.cloudRecommendation;

import jakarta.validation.constraints.NotBlank;

public record UpdateCloudStatusRequest(
        @NotBlank(message = "El estado no puede estar vacío")
        String status
) {}
