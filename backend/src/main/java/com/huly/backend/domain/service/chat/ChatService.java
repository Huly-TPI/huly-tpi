package com.huly.backend.domain.service.chat;

import java.util.List;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.ChatPersonalizationContext;
import com.huly.backend.domain.model.chat.ChatRecommendationOutcome;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatUserIntent;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;
    private final UserVectorMemoryService userVectorMemoryService;
    private final ChatEmotionalRecommendationService chatEmotionalRecommendationService;
    private final ChatIntentDetectionService chatIntentDetectionService;
    private final ChatQuotaService chatQuotaService;
    private final UserRepository userRepository;
    private final ChatConversationPreferenceRepository chatConversationPreferenceRepository;

    public ChatReply processMessage(String message, String conversationId, Long userId) {
        return processMessage(message, conversationId, userId, false);
    }

    public ChatReply processMessage(
            String message,
            String conversationId,
            Long userId,
            boolean offerCommunicationStyleWhenSafe) {
        chatQuotaService.assertWithinLimit(userId);
        ChatContext context = buildBlockingContext(message, conversationId, userId);
        ChatUserIntent userIntent = chatIntentDetectionService.detect(message);
        ChatRecommendationOutcome recommendationOutcome = evaluateRecommendation(
                message,
                userId,
                context,
                userIntent);
        var suggestedAction = recommendationOutcome != null ? recommendationOutcome.suggestedAction() : null;
        context = context.withSystemPrompt(promptBuilderService.buildEnrichedPrompt(
                context.basePrompt(),
                context.riskWords(),
                context.memories(),
                suggestedAction,
                userIntent,
                context.personalization()));

        ChatReply reply = llmChatPort.chat(context.systemPrompt(), message, context.history());
        reply = ensureRequestedChallenge(reply, userIntent, suggestedAction);
        ChatReply finalReply = applyRecommendationOutcome(conversationId, userId, reply, recommendationOutcome);
        finalReply = appendCommunicationStyleQuestionIfSafe(
                finalReply,
                userId,
                offerCommunicationStyleWhenSafe);
        saveUserMessage(conversationId, message, finalReply, userId);
        saveAssistantMessage(conversationId, finalReply.content(), userId);
        userVectorMemoryService.rememberChatMessage(userId, conversationId, message);

        return finalReply;
    }

    private ChatRecommendationOutcome evaluateRecommendation(
            String message,
            Long userId,
            ChatContext context,
            ChatUserIntent userIntent) {
        if (userIntent == ChatUserIntent.CHALLENGE_REQUEST) {
            return ChatRecommendationOutcome.none(EmotionalAnalysisResult.neutral());
        }
        return chatEmotionalRecommendationService.evaluate(
                message,
                userId,
                context.basePrompt(),
                context.memories(),
                context.history(),
                null,
                userIntent == ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    private ChatContext buildBlockingContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        List<RiskWord> riskWords = riskWordRepository.findAllActive();
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId, userId);
        ChatPersonalizationContext personalization = loadPersonalizationContext(userId);
        return new ChatContext(basePrompt, null, riskWords, memories, history, personalization);
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
        if (!offerCommunicationStyleWhenSafe
                || Boolean.TRUE.equals(reply.riskDetected())
                || (reply.intensity() != null && reply.intensity() >= 7)
                || reply.suggestedAction() != null) {
            return reply;
        }

        ChatConversationPreference pendingPreference = chatConversationPreferenceRepository.findByUserId(userId)
                .filter(preference -> preference.getOnboardingStatus()
                        == com.huly.backend.domain.model.enums.ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
                .orElse(null);
        if (pendingPreference == null) {
            return reply;
        }
        chatConversationPreferenceRepository.save(
                pendingPreference.markCommunicationStyleAsked(Instant.now()));

        String content = reply.content() == null || reply.content().isBlank()
                ? CommunicationStyle.QUESTION_TEXT
                : reply.content().trim() + "\n\n" + CommunicationStyle.QUESTION_TEXT;
        return new ChatReply(
                content,
                reply.detectedEmotion(),
                reply.intensity(),
                reply.riskDetected(),
                reply.matchedWord(),
                reply.suggestedAction(),
                reply.generatedChallenge());
    }

    private ChatReply applyRecommendationOutcome(
            String conversationId,
            Long userId,
            ChatReply reply,
            ChatRecommendationOutcome outcome) {
        ChatReply enriched = applyAnalysisMetadata(reply, outcome != null ? outcome.analysis() : null);

        if (outcome == null || outcome.suggestedAction() == null) {
            userVectorMemoryService.rememberGeneratedChallenge(userId, conversationId, enriched.generatedChallenge());
            return enriched;
        }

        userVectorMemoryService.rememberRecommendedActivity(
                userId,
                conversationId,
                outcome.suggestedAction().emotionalEventId(),
                outcome.suggestedAction());
        return new ChatReply(
                enriched.content(),
                enriched.detectedEmotion(),
                enriched.intensity(),
                enriched.riskDetected(),
                enriched.matchedWord(),
                outcome.suggestedAction(),
                null);
    }

    private ChatReply ensureRequestedChallenge(
            ChatReply reply,
            ChatUserIntent userIntent,
            Object suggestedAction) {
        if (userIntent != ChatUserIntent.CHALLENGE_REQUEST
                || suggestedAction != null
                || reply.generatedChallenge() != null) {
            return reply;
        }

        ChatReply.GeneratedChallenge challenge = new ChatReply.GeneratedChallenge(
                "Reto de accion pequena",
                "Elegí una acción simple que puedas hacer en los próximos 10 minutos y realizala sin buscar que salga perfecta."
        );
        String content = reply.content() == null || reply.content().isBlank()
                ? "Te propongo un reto simple para empezar: elegí una acción pequeña que puedas hacer en los próximos 10 minutos y hacela sin buscar que salga perfecta."
                : reply.content() + " Te propongo este reto: elegí una acción pequeña que puedas hacer en los próximos 10 minutos y hacela sin buscar que salga perfecta.";
        return new ChatReply(
                content,
                reply.detectedEmotion(),
                reply.intensity(),
                reply.riskDetected(),
                reply.matchedWord(),
                reply.suggestedAction(),
                challenge);
    }

    private ChatReply applyAnalysisMetadata(ChatReply reply, EmotionalAnalysisResult analysis) {
        if (analysis == null || analysis.detectedEmotion() == null || analysis.confidence() <= 0.0) {
            return reply;
        }
        return reply.withEmotionalMetadata(analysis.detectedEmotion(), toChatIntensity(analysis.intensity()));
    }

    private Integer toChatIntensity(double intensity) {
        return (int) Math.round(Math.max(0.0, Math.min(1.0, intensity)) * 10.0);
    }

    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                    MessageRole.USER,
                    message,
                    reply.detectedEmotion(),
                    reply.riskDetected(),
                    reply.matchedWord()), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private void saveAssistantMessage(String conversationId, String content, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, ConversationMessage.of(MessageRole.ASSISTANT, content), userId);
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
