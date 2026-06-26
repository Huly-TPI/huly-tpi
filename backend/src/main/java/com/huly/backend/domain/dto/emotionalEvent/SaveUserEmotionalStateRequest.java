package com.huly.backend.domain.dto.emotionalEvent;

/**
 * Pedido de dominio para guardar el estado emocional de un usuario.
 */
public record SaveUserEmotionalStateRequest(
        Long userId,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String source
) {
}
