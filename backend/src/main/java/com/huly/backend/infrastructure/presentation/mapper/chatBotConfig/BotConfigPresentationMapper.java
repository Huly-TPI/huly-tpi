package com.huly.backend.infrastructure.presentation.mapper.chatBotConfig;

import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigRequest;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.BotConfigResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de configuracion del bot:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class BotConfigPresentationMapper {

    public UpdateBotConfigRequest toUpdateRequest(
            com.huly.backend.infrastructure.presentation.dto.chatConfig.UpdateBotConfigRequest request
    ) {
        return new UpdateBotConfigRequest(
                request.riskDetectionEnabled(),
                request.systemPrompt(),
                request.preferredNameQuestionEnabled(),
                request.communicationStyleQuestionEnabled()
        );
    }

    public BotConfigResponse toResponse(GetBotConfigResponse response) {
        return new BotConfigResponse(
                response.id(),
                response.riskDetectionEnabled(),
                response.systemPrompt(),
                response.preferredNameQuestionEnabled(),
                response.communicationStyleQuestionEnabled()
        );
    }

    public BotConfigResponse toResponse(UpdateBotConfigResponse response) {
        return new BotConfigResponse(
                response.id(),
                response.riskDetectionEnabled(),
                response.systemPrompt(),
                response.preferredNameQuestionEnabled(),
                response.communicationStyleQuestionEnabled()
        );
    }
}
