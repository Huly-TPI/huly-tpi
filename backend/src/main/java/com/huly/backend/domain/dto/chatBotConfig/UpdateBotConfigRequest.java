package com.huly.backend.domain.dto.chatBotConfig;

/**
 * Pedido de dominio para actualizar la configuracion del bot de chat.
 */
public record UpdateBotConfigRequest(
        Boolean riskDetectionEnabled,
        String systemPrompt,
        Boolean preferredNameQuestionEnabled,
        Boolean communicationStyleQuestionEnabled
) {
}
