package com.huly.backend.domain.mapper.chatBotConfig;

import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigRequest;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.UpdateBotConfigCommand;

/**
 * Mapper de dominio para el caso de uso de actualizacion de la configuracion del bot.
 */
public class UpdateBotConfigMapper {

    public UpdateBotConfigCommand toModel(UpdateBotConfigRequest request) {
        return new UpdateBotConfigCommand(
                request.riskDetectionEnabled(),
                request.systemPrompt(),
                request.preferredNameQuestionEnabled(),
                request.communicationStyleQuestionEnabled()
        );
    }

    public UpdateBotConfigResponse toResponse(ChatConfig config) {
        return new UpdateBotConfigResponse(
                config.getId(),
                config.getRiskDetectionEnabled(),
                config.getSystemPrompt(),
                config.getPreferredNameQuestionEnabled(),
                config.getCommunicationStyleQuestionEnabled()
        );
    }
}
