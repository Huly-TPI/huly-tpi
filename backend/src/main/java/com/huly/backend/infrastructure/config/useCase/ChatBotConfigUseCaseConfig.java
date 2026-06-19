package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.service.chat.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfigUseCaseConfig {

    @Bean
    public GetBotConfigUseCase getBotConfigUseCase(BotConfigService botConfigService) {
        return new GetBotConfigUseCase(botConfigService);
    }

    @Bean
    public UpdateBotConfigUseCase updateBotConfigUseCase(BotConfigService botConfigService) {
        return new UpdateBotConfigUseCase(botConfigService);
    }
}
