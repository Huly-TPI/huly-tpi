package com.huly.backend.infrastructure.presentation.dto.breathingTechniques;

public record AdminBreathingTechniqueResponse(
        Long id, String name, String description,
        int inhaleSeconds, int holdSeconds, int exhaleSeconds,
        int roundsInterval, int rounds, boolean active
) {}