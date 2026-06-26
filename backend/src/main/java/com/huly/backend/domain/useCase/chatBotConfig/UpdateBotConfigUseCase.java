package com.huly.backend.domain.useCase.chatBotConfig;

import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigRequest;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.mapper.chatBotConfig.UpdateBotConfigMapper;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.chat.BotConfigService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateBotConfigUseCase {

    private final BotConfigService botConfigService;
    private final UpdateBotConfigMapper mapper;

    public UpdateBotConfigResponse execute(UpdateBotConfigRequest request) {
        ChatConfig updated = botConfigService.updateConfig(mapper.toModel(request));
        return mapper.toResponse(updated);
    }
}
