package com.huly.backend.domain.dto.BreathingSession;

/**
 * Representacion de una tecnica de respiracion dentro de la respuesta de dominio.
 */
public record BreathingTechniqueItem(
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
