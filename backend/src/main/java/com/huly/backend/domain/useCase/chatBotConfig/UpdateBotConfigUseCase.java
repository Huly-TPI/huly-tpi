package com.huly.backend.domain.useCase.chatBotConfig;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.UpdateBotConfigCommand;
import com.huly.backend.domain.service.BotConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateBotConfigUseCase {

    private final BotConfigService botConfigService;

    public ChatConfig execute(UpdateBotConfigCommand command) {
        return botConfigService.updateConfig(command);
    }
}