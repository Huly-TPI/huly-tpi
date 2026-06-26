package com.huly.backend.domain.dto.emotionalEvent;

/**
 * Pedido de dominio para actualizar el feedback de un evento emocional.
 */
public record UpdateEmotionalEventFeedbackRequest(
        Long eventId,
        Integer feedbackScore,
        String feedbackText
) {
}
