package com.huly.backend.infrastructure.presentation.dto.breathingTechniques;

import jakarta.validation.constraints.NotBlank;

public record BreathingTechniqueWebRequest(
        @NotBlank String name,
        String description,
        int inhaleSeconds,
        int holdSeconds,
        int exhaleSeconds,
        int roundsInterval,
        int rounds
) {}