package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.chat.*;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.port.ChatMemoryPort;
import com.huly.backend.domain.port.LLMChatPort;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ChatUseCase {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;
    private final UserVectorMemoryService userVectorMemoryService;
    private final GetChatEmotionalRecommendationUseCase getChatEmotionalRecommendationUseCase;
    private final ChatQuotaService chatQuotaService;
    private final UserRepository userRepository;
    private final ChatConversationPreferenceRepository chatConversationPreferenceRepository;
    private final HandleChatPreferencesUseCase handleChatPreferencesUseCase;
    private final ChatMapper mapper;

    public ChatReply execute(String message, String conversationId, Long userId) {
        ChatPreferenceHandlingResult preferenceResult =
                handleChatPreferencesUseCase.execute(userId, conversationId, message);
        if (!preferenceResult.continueConversation()) {
            return preferenceResult.directReply();
        }

        ChatUserIntent userIntent = ChatUserIntent.detect(message);

        return processMessage(
                message,
                conversationId,
                userId,
                preferenceResult.offerCommunicationStyleWhenSafe(),
                userIntent);
    }

    private ChatReply processMessage(
            String message,
            String conversationId,
            Long userId,
            boolean offerCommunicationStyleWhenSafe,
            ChatUserIntent userIntent) {
        chatQuotaService.assertWithinLimit(userId);
        ChatContext context = loadChatContext(message, conversationId, userId);
        ChatRecommendationOutcome recommendationOutcome = evaluateRecommendation(
                message,
                userId,
                context,
                userIntent);
        SuggestedChatAction suggestedAction = recommendationOutcome != null
                ? recommendationOutcome.suggestedAction()
                : null;
        context = context.withSystemPrompt(buildSystemPrompt(context, suggestedAction, userIntent));

        ChatReply reply = llmChatPort.chat(context.systemPrompt(), message, context.history());
        reply = ensureRequestedChallenge(reply, userIntent, suggestedAction);

        if (ChatUserIntent.isChallengeResponse(message)) {
            reply = reply.withoutGeneratedChallenge();
        }

        ChatReply finalReply = applyRecommendationOutcome(conversationId, userId, reply, recommendationOutcome);
        finalReply = appendCommunicationStyleQuestionIfSafe(
                finalReply,
                userId,
                offerCommunicationStyleWhenSafe);
        saveConversationExchange(conversationId, message, finalReply, userId);

        rememberChatMessage(userId, conversationId, message);

        return finalReply;
    }

    private void rememberChatMessage(Long userId, String conversationId, String message) {
        userVectorMemoryService.saveMemory(mapper.toUserMemoryCommand(userId, conversationId, message));
    }

    private ChatRecommendationOutcome evaluateRecommendation(
            String message,
            Long userId,
            ChatContext context,
            ChatUserIntent userIntent) {
        if (userIntent == ChatUserIntent.CHALLENGE_REQUEST) {
            return ChatRecommendationOutcome.none(EmotionalAnalysisResult.neutral());
        }
        return getChatEmotionalRecommendationUseCase.execute(
                message,
                userId,
                context.basePrompt(),
                context.memories(),
                context.history(),
                null,
                userIntent == ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    private ChatContext loadChatContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId, userId);
        ChatPersonalizationContext personalization = loadPersonalizationContext(userId);
        return new ChatContext(basePrompt, null, riskWords, memories, history, personalization);
    }

    private String buildSystemPrompt(
            ChatContext context,
            SuggestedChatAction suggestedAction,
            ChatUserIntent userIntent) {
        return promptBuilderService.buildEnrichedPrompt(
                context.basePrompt(),
                context.riskWords(),
                context.memories(),
                suggestedAction,
                userIntent,
                context.personalization());
    }

    private ChatPersonalizationContext loadPersonalizationContext(Long userId) {
        String registeredName = userRepository.findById(userId)
                .map(AppUser::getName)
                .orElse(null);
        ChatConversationPreference preference =
                chatConversationPreferenceRepository.findByUserId(userId).orElse(null);
        return new ChatPersonalizationContext(
                registeredName,
                preference != null ? preference.getPreferredName() : null,
                preference != null ? preference.getCommunicationStyle() : null);
    }

    private String basePrompt() {
        return chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");
    }

    private ChatReply appendCommunicationStyleQuestionIfSafe(
            ChatReply reply,
            Long userId,
            boolean offerCommunicationStyleWhenSafe) {
        if (!offerCommunicationStyleWhenSafe || !reply.canOfferCommunicationStyle()) {
            return reply;
        }

        ChatConversationPreference pendingPreference = chatConversationPreferenceRepository.findByUserId(userId)
                .filter(ChatConversationPreference::isPendingCommunicationStyle)
                .orElse(null);
        if (pendingPreference == null) {
            return reply;
        }
        chatConversationPreferenceRepository.save(
                pendingPreference.markCommunicationStyleAsked(Instant.now()));

        return reply.appendContent(CommunicationStyle.QUESTION_TEXT);
    }

    private ChatReply applyRecommendationOutcome(
            String conversationId,
            Long userId,
            ChatReply reply,
            ChatRecommendationOutcome outcome) {
        ChatReply enriched = applyAnalysisMetadata(reply, outcome != null ? outcome.analysis() : null);

        if (outcome == null || outcome.suggestedAction() == null) {
            rememberGeneratedChallenge(userId, conversationId, enriched.generatedChallenge());
            return enriched;
        }

        rememberRecommendedActivity(userId, conversationId, outcome.suggestedAction().emotionalEventId(), outcome.suggestedAction());

        return enriched.withSuggestedAction(outcome.suggestedAction()).withoutGeneratedChallenge();
    }

    private void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        if (challenge == null || challenge.title() == null || challenge.title().isBlank()) {
            return;
        }
        userVectorMemoryService.saveMemory(
                mapper.toGeneratedChallengeCommand(userId, conversationId, challenge));
    }

    private void rememberRecommendedActivity(Long userId, String conversationId, Long emotionalEventId, SuggestedChatAction action) {
        if (action == null) {
            return;
        }
        userVectorMemoryService.saveMemory(
                mapper.toRecommendedActivityCommand(userId, conversationId, emotionalEventId, action));
    }

    private ChatReply ensureRequestedChallenge(
            ChatReply reply,
            ChatUserIntent userIntent,
            SuggestedChatAction suggestedAction) {
        if (userIntent != ChatUserIntent.CHALLENGE_REQUEST
                || suggestedAction != null
                || reply.generatedChallenge() != null) {
            return reply;
        }

        String content = reply.content() == null || reply.content().isBlank()
                ? "Te propongo un reto simple para empezar: elegí una acción pequeña que puedas hacer en los próximos 10 minutos y hacela sin buscar que salga perfecta."
                : reply.content() + " Te propongo este reto: elegí una acción pequeña que puedas hacer en los próximos 10 minutos y hacela sin buscar que salga perfecta.";
        return reply.withContent(content)
                .withGeneratedChallenge(ChatReply.GeneratedChallenge.defaultActionChallenge());
    }

    private ChatReply applyAnalysisMetadata(ChatReply reply, EmotionalAnalysisResult analysis) {
        if (analysis == null || !analysis.hasUsableEmotion()) {
            return reply;
        }
        return reply.withEmotionalMetadata(analysis.detectedEmotion(), analysis.chatIntensity());
    }

    private void saveConversationExchange(String conversationId, String message, ChatReply reply, Long userId) {
        saveUserMessage(conversationId, message, reply, userId);
        saveAssistantMessage(conversationId, reply, userId);
    }

    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, mapper.toUserMessage(message, reply), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private void saveAssistantMessage(String conversationId, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, mapper.toAssistantMessage(reply), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje del asistente userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private record ChatContext(
            String basePrompt,
            String systemPrompt,
            List<RiskWord> riskWords,
            List<VectorMemory> memories,
            List<ConversationMessage> history,
            ChatPersonalizationContext personalization) {
        private ChatContext withSystemPrompt(String nextSystemPrompt) {
            return new ChatContext(
                    basePrompt,
                    nextSystemPrompt,
                    riskWords,
                    memories,
                    history,
                    personalization);
        }
    }
}
