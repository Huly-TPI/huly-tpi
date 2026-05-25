package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.BotConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetBotConfigUseCase {

    private final BotConfigService botConfigService;

    public ChatConfig execute() {
        return botConfigService.getConfig();
    }
}