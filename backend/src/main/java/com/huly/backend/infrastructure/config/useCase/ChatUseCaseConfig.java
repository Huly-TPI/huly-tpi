package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.service.chat.ChatService;
import com.huly.backend.domain.useCase.chat.ChatUseCase;
import com.huly.backend.domain.useCase.chat.HandleChatPreferencesUseCase;
import com.huly.backend.domain.useCase.chat.InitializeChatPreferencesUseCase;
import com.huly.backend.domain.useCase.chat.ListChatHistoryUseCase;
import com.huly.backend.domain.useCase.chat.StreamChatUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatUseCaseConfig {

    @Bean
    public ChatUseCase chatUseCase(
            ChatService chatService,
            HandleChatPreferencesUseCase handleChatPreferencesUseCase
    ) {
        return new ChatUseCase(chatService, handleChatPreferencesUseCase);
    }

    @Bean
    public StreamChatUseCase streamChatUseCase(ChatService chatService) {
        return new StreamChatUseCase(chatService);
    }

    @Bean
    public ListChatHistoryUseCase listChatHistoryUseCase(
            ChatMessageRepository chatMessageRepository,
            InitializeChatPreferencesUseCase initializeChatPreferencesUseCase
    ) {
        return new ListChatHistoryUseCase(
                chatMessageRepository,
                initializeChatPreferencesUseCase
        );
    }
}
