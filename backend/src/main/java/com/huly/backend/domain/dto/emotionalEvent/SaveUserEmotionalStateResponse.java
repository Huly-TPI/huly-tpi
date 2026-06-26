package com.huly.backend.domain.dto.emotionalEvent;

import java.time.Instant;

/**
 * Respuesta de dominio luego de guardar el estado emocional de un usuario.
 */
public record SaveUserEmotionalStateResponse(
        Long id,
        Long userId,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String source,
        Instant timestamp
) {
}
