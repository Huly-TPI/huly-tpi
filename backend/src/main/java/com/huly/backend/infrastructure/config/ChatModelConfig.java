package com.huly.backend.infrastructure.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
    ChatModel anthropicPrimaryModel(AnthropicChatModel model) {
        return model;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
    ChatModel ollamaPrimaryModel(OllamaChatModel model) {
        return model;
    }
}
