package com.huly.backend.domain.useCase.chatBotConfig;

import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.mapper.chatBotConfig.GetBotConfigMapper;
import com.huly.backend.domain.service.chat.BotConfigService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBotConfigUseCase {

    private final BotConfigService botConfigService;
    private final GetBotConfigMapper mapper;

    public GetBotConfigResponse execute() {
        return mapper.toResponse(botConfigService.getConfig());
    }
}
