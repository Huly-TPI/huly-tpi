package com.huly.backend.domain.dto.chat;

import com.huly.backend.domain.model.enums.EmotionType;

/**
 * Response de dominio del chat: los datos de la respuesta del asistente, con DTOs propios
 * para la acción sugerida y el reto (sin exponer tipos del model).
 */
public record ChatReplyResponse(
        String content,
        EmotionType detectedEmotion,
        Integer intensity,
        Boolean riskDetected,
        String matchedWord,
        SuggestedActionResponse suggestedAction,
        GeneratedChallengeResponse generatedChallenge
) {
}
