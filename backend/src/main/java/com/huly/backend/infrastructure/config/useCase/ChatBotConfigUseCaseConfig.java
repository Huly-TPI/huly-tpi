package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.chatBotConfig.GetBotConfigMapper;
import com.huly.backend.domain.mapper.chatBotConfig.UpdateBotConfigMapper;
import com.huly.backend.domain.service.chat.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfigUseCaseConfig {

    @Bean
    public GetBotConfigMapper getBotConfigMapper() {
        return new GetBotConfigMapper();
    }

    @Bean
    public UpdateBotConfigMapper updateBotConfigMapper() {
        return new UpdateBotConfigMapper();
    }

    @Bean
    public GetBotConfigUseCase getBotConfigUseCase(BotConfigService botConfigService, GetBotConfigMapper getBotConfigMapper) {
        return new GetBotConfigUseCase(botConfigService, getBotConfigMapper);
    }

    @Bean
    public UpdateBotConfigUseCase updateBotConfigUseCase(BotConfigService botConfigService, UpdateBotConfigMapper updateBotConfigMapper) {
        return new UpdateBotConfigUseCase(botConfigService, updateBotConfigMapper);
    }
}
