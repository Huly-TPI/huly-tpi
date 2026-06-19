package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.chat.*;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatUseCaseConfig {

    @Bean
    public InitializeChatPreferencesUseCase initializeChatPreferencesUseCase(
            ChatConversationPreferenceRepository preferenceRepository,
            UserRepository userRepository,
            ChatMemoryPort chatMemoryPort,
            ChatConfigRepository chatConfigRepository
    ) {
        return new InitializeChatPreferencesUseCase(
                preferenceRepository,
                userRepository,
                chatMemoryPort,
                chatConfigRepository
        );
    }

    @Bean
    public HandleChatPreferencesUseCase handleChatPreferencesUseCase(
            ChatConversationPreferenceRepository preferenceRepository,
            ChatPreferenceExtractionPort extractionPort,
            InitializeChatPreferencesUseCase initializeChatPreferencesUseCase,
            ChatMemoryPort chatMemoryPort,
            ChatQuotaService chatQuotaService,
            ChatConfigRepository chatConfigRepository
    ) {
        return new HandleChatPreferencesUseCase(
                preferenceRepository,
                extractionPort,
                initializeChatPreferencesUseCase,
                chatMemoryPort,
                chatQuotaService,
                chatConfigRepository
        );
    }

    @Bean
    public GetChatEmotionalRecommendationUseCase getChatEmotionalRecommendationUseCase(
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            GetEmotionalRecommendationsUseCase recommendationsUseCase,
            CreateEmotionalEventUseCase createEmotionalEventUseCase
    ) {
        return new GetChatEmotionalRecommendationUseCase(
                emotionalAnalysisPort,
                promptBuilderService,
                recommendationPolicy,
                recommendationsUseCase,
                createEmotionalEventUseCase
        );
    }

    @Bean
    public ChatUseCase chatUseCase(
            LLMChatPort llmChatPort,
            ChatMemoryPort chatMemoryPort,
            ChatConfigRepository chatConfigRepository,
            RiskWordRepository riskWordRepository,
            PromptBuilderService promptBuilderService,
            UserVectorMemoryService userVectorMemoryService,
            GetChatEmotionalRecommendationUseCase getChatEmotionalRecommendationUseCase,
            ChatQuotaService chatQuotaService,
            UserRepository userRepository,
            ChatConversationPreferenceRepository chatConversationPreferenceRepository,
            HandleChatPreferencesUseCase handleChatPreferencesUseCase
    ) {
        return new ChatUseCase(
                llmChatPort,
                chatMemoryPort,
                chatConfigRepository,
                riskWordRepository,
                promptBuilderService,
                userVectorMemoryService,
                getChatEmotionalRecommendationUseCase,
                chatQuotaService,
                userRepository,
                chatConversationPreferenceRepository,
                handleChatPreferencesUseCase
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
            InitializeChatPreferencesUseCase initializeChatPreferencesUseCase
    ) {
        return new ListChatHistoryUseCase(
                chatMessageRepository,
                initializeChatPreferencesUseCase
        );
    }

    @Bean
    public SaveChallengeDecisionUseCase saveChallengeDecisionUseCase(
            UserVectorMemoryService userVectorMemoryService
    ) {
        return new SaveChallengeDecisionUseCase(userVectorMemoryService);
    }
}
