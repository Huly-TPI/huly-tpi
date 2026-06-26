package com.huly.backend.domain.dto.chatBotConfig;

/**
 * Respuesta de dominio con la configuracion del bot de chat.
 */
public record GetBotConfigResponse(
        Long id,
        Boolean riskDetectionEnabled,
        String systemPrompt,
        Boolean preferredNameQuestionEnabled,
        Boolean communicationStyleQuestionEnabled
) {
}
