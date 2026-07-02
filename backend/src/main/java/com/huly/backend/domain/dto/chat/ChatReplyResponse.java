package com.huly.backend.domain.dto.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.EmotionType;

/**
 * Response de dominio del chat: los datos de la respuesta del asistente, sin exponer el
 * agregado {@link ChatReply} ni sus métodos de transformación.
 */
public record ChatReplyResponse(
        String content,
        EmotionType detectedEmotion,
        Integer intensity,
        Boolean riskDetected,
        String matchedWord,
        SuggestedChatAction suggestedAction,
        ChatReply.GeneratedChallenge generatedChallenge
) {
}
