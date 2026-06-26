package com.huly.backend.domain.dto.emotionalEvent;

import com.huly.backend.domain.model.enums.RecommendationDecision;

/**
 * Pedido de dominio para actualizar la decision de recomendacion de un evento emocional.
 */
public record UpdateEmotionalEventDecisionRequest(
        Long eventId,
        RecommendationDecision decision,
        Long chosenActivityId
) {
}
