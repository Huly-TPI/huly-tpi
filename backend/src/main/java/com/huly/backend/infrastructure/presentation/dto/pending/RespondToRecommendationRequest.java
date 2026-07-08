package com.huly.backend.infrastructure.presentation.dto.pending;

import jakarta.validation.constraints.NotBlank;

public record RespondToRecommendationRequest(
        @NotBlank(message = "La decisión no puede estar vacía")
        String decision
) {}
