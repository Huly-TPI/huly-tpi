package com.huly.backend.domain.mapper.chatBotConfig;

import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.model.chat.ChatConfig;

/**
 * Mapper de dominio para el caso de uso de obtencion de la configuracion del bot.
 */
public class GetBotConfigMapper {

    public GetBotConfigResponse toResponse(ChatConfig config) {
        return new GetBotConfigResponse(
                config.getId(),
                config.getRiskDetectionEnabled(),
                config.getSystemPrompt(),
                config.getPreferredNameQuestionEnabled(),
                config.getCommunicationStyleQuestionEnabled()
        );
    }
}
