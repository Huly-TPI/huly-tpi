package com.huly.backend.domain.dto.breathingTechnique;

public record UpdateBreathingTechniqueRequest(
        Long id, String name, String description,
        int inhaleSeconds, int holdSeconds, int exhaleSeconds,
        int roundsInterval, int rounds
) {}