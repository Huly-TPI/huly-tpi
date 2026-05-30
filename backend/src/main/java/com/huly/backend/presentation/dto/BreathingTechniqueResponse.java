package com.huly.backend.presentation.dto;

public record BreathingTechniqueResponse(
        Long id,
        String name,
        String description,
        int inhaleSeconds,
        int holdSeconds,
        int exhaleSeconds,
        int roundsInterval,
        int rounds
) {
}