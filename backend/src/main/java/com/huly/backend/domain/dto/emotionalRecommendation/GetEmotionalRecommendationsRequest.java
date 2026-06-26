package com.huly.backend.domain.dto.emotionalRecommendation;

/**
 * Pedido de dominio para obtener recomendaciones emocionales de actividades.
 *
 * @param userId    usuario que solicita las recomendaciones.
 * @param valence   valencia emocional actual.
 * @param arousal   activacion emocional actual.
 * @param dominance dominancia emocional actual.
 * @param intensity intensidad emocional actual.
 * @param userGoal  objetivo expresado por el usuario.
 */
public record GetEmotionalRecommendationsRequest(
        Long userId,
        double valence,
        double arousal,
        double dominance,
        double intensity,
        String userGoal
) {
}
