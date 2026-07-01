package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.port.LLMChatPort;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatPreferenceHandlingService;
import com.huly.backend.domain.service.chat.ChatPreferenceInitializationService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.useCase.chat.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatUseCaseConfig {

    @Bean
    public ChatMapper chatMapper() {
        return new ChatMapper();
    }

    @Bean
    public ChatUseCase chatUseCase(
            LLMChatPort llmChatPort,
            ChatMemoryPort chatMemoryPort,
            ChatConfigRepository chatConfigRepository,
            RiskWordRepository riskWordRepository,
            PromptBuilderService promptBuilderService,
            UserVectorMemoryService userVectorMemoryService,
            ChatEmotionalRecommendationService chatEmotionalRecommendationService,
            ChatQuotaService chatQuotaService,
            UserRepository userRepository,
            ChatConversationPreferenceRepository chatConversationPreferenceRepository,
            ChatPreferenceHandlingService chatPreferenceHandlingService,
            ChatMapper chatMapper
    ) {
        return new ChatUseCase(
                llmChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                userVectorMemoryService,
                chatEmotionalRecommendationService,
                chatQuotaService,
                userRepository,
                chatConversationPreferenceRepository,
                chatPreferenceHandlingService,
                chatMapper
        );
    }

    @Bean
    public AudioChatUseCase audioChatUseCase(
            AudioTranscriptionPort audioTranscriptionPort,
            ChatUseCase chatUseCase
    ) {
        return new AudioChatUseCase(audioTranscriptionPort, chatUseCase);
    }

    @Bean
    public ListChatHistoryUseCase listChatHistoryUseCase(
            ChatMessageRepository chatMessageRepository,
            ChatPreferenceInitializationService chatPreferenceInitializationService
    ) {
        return new ListChatHistoryUseCase(
                chatMessageRepository,
                chatPreferenceInitializationService
        );
    }

    @Bean
    public SaveChallengeDecisionUseCase saveChallengeDecisionUseCase(
            UserVectorMemoryService userVectorMemoryService,
            ChatMessageRepository chatMessageRepository
    ) {
        return new SaveChallengeDecisionUseCase(userVectorMemoryService, chatMessageRepository);
    }
}
