package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatMessageRequest;
import com.huly.backend.domain.dto.chat.ChatReplyResponse;
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
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chatBotConfig.RiskWordRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatPreferenceHandlingService;
import com.huly.backend.domain.service.chat.ChatQuotaService;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Caso de uso principal del chatbot. Resuelve primero un posible cambio de preferencia
 * (nombre/estilo) y, si la conversación continúa, arma el contexto, evalúa si corresponde
 * recomendar una actividad, genera la respuesta del LLM y persiste el turno.
 */
@Slf4j
@RequiredArgsConstructor
public class ChatUseCase {

    private final LLMChatPort llmChatPort;
    private final ChatMemoryPort chatMemoryPort;
    private final ChatConfigRepository chatConfigRepository;
    private final RiskWordRepository riskWordRepository;
    private final PromptBuilderService promptBuilderService;
    private final UserVectorMemoryService userVectorMemoryService;
    private final ChatEmotionalRecommendationService chatEmotionalRecommendationService;
    private final ChatQuotaService chatQuotaService;
    private final UserRepository userRepository;
    private final ChatConversationPreferenceRepository chatConversationPreferenceRepository;
    private final ChatPreferenceHandlingService chatPreferenceHandlingService;
    private final ChatMapper mapper;
    private final ChatMessageRepository chatMessageRepository;

    // Punto de entrada del chat: atiende cambios de preferencia y, si sigue, procesa el mensaje.
    public ChatReplyResponse execute(ChatMessageRequest request) {
        Long userId = request.userId();
        String conversationId = request.conversationId();
        String message = request.message();

        ChatPreferenceHandlingResult preferenceResult =
                chatPreferenceHandlingService.handle(userId, conversationId, message);
        if (!preferenceResult.continueConversation()) {
            return mapper.toChatReplyResponse(preferenceResult.directReply());
        }

        ChatUserIntent userIntent = ChatUserIntent.detect(message);
        ChatReply reply = processMessage(
                message,
                conversationId,
                userId,
                preferenceResult.offerCommunicationStyleWhenSafe(),
                userIntent);
        return mapper.toChatReplyResponse(reply);
    }

    // Pipeline del mensaje: cuota → contexto → recomendación → respuesta → persistencia.
    private ChatReply processMessage(
            String message,
            String conversationId,
            Long userId,
            boolean offerCommunicationStyleWhenSafe,
            ChatUserIntent userIntent) {
        chatQuotaService.assertWithinLimit(userId);
        ChatContext context = loadChatContext(message, conversationId, userId);
        ChatRecommendationOutcome recommendationOutcome = evaluateRecommendation(message, userId, context, userIntent);
        ChatReply reply = generateReply(context, message, userIntent, recommendationOutcome);
        ChatReply recommendedReply = applyRecommendationOutcome(conversationId, userId, reply, recommendationOutcome);
        ChatReply finalReply = appendCommunicationStyleQuestionIfSafe(recommendedReply, userId, offerCommunicationStyleWhenSafe);
        recordConversationTurn(conversationId, message, finalReply, userId);
        return finalReply;
    }

    // Genera la respuesta del LLM con el prompt enriquecido y ajusta el reto según el intent.
    private ChatReply generateReply(
            ChatContext context,
            String message,
            ChatUserIntent userIntent,
            ChatRecommendationOutcome recommendationOutcome) {
        SuggestedChatAction suggestedAction = recommendationOutcome != null
                ? recommendationOutcome.suggestedAction()
                : null;
        ChatContext enriched = context.withSystemPrompt(buildSystemPrompt(context, suggestedAction, userIntent));
        ChatReply reply = llmChatPort.chat(enriched.systemPrompt(), message, enriched.history());
        reply = ensureRequestedChallenge(reply, userIntent, suggestedAction);
        return ChatUserIntent.isChallengeResponse(message)
                ? reply.withoutGeneratedChallenge()
                : reply;
    }

    // Guarda el mensaje del usuario en la memoria vectorial.
    private void rememberChatMessage(Long userId, String conversationId, String message) {
        userVectorMemoryService.saveMemory(mapper.toUserMemoryCommand(userId, conversationId, message));
    }

    // Decide si corresponde recomendar una actividad emocional (salvo pedido explícito de reto).
    private ChatRecommendationOutcome evaluateRecommendation(
            String message,
            Long userId,
            ChatContext context,
            ChatUserIntent userIntent) {
        if (userIntent == ChatUserIntent.CHALLENGE_REQUEST) {
            return ChatRecommendationOutcome.none(EmotionalAnalysisResult.neutral());
        }
        return chatEmotionalRecommendationService.recommend(
                message,
                userId,
                context.basePrompt(),
                context.memories(),
                context.history(),
                null,
                userIntent == ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    // Reúne prompt base, memorias, palabras de riesgo, historial y personalización.
    private ChatContext loadChatContext(String message, String conversationId, Long userId) {
        String basePrompt = basePrompt();
        List<VectorMemory> memories = userVectorMemoryService.findRelevantUserMemories(userId, message);
        
        boolean riskDetectionEnabled = chatConfigRepository.findFirst()
                .map(ChatConfig::getRiskDetectionEnabled)
                .orElse(true);
        List<RiskWord> riskWords = riskDetectionEnabled 
                ? riskWordRepository.findAllActive() 
                : List.of();
                
        List<ConversationMessage> history = chatMemoryPort.getHistory(conversationId, userId);
        List<ChatPersonalizationContext.ChallengeHistoryEntry> challengeHistory = loadChallengeHistory(userId);
        ChatPersonalizationContext personalization = loadPersonalizationContext(userId, challengeHistory);
        return new ChatContext(basePrompt, null, riskWords, memories, history, personalization);
    }

    private List<ChatPersonalizationContext.ChallengeHistoryEntry> loadChallengeHistory(Long userId) {
        List<ConversationMessage> recentChallenges = chatMessageRepository.findRecentChallengesByUserId(userId, 50);

        return recentChallenges.stream()
                .filter(this::hasValidChallenge)
                .collect(Collectors.groupingBy(this::normalizeChallengeTitle))
                .values().stream()
                .map(this::toChallengeHistoryEntry)
                .toList();
    }

    private boolean hasValidChallenge(ConversationMessage msg) {
        return msg != null && msg.generatedChallenge() != null && msg.generatedChallenge().title() != null;
    }

    private String normalizeChallengeTitle(ConversationMessage msg) {
        return msg.generatedChallenge().title().trim().toLowerCase();
    }

    private ChatPersonalizationContext.ChallengeHistoryEntry toChallengeHistoryEntry(List<ConversationMessage> messages) {
        String originalTitle = messages.get(0).generatedChallenge().title().trim();

        long accepted = messages.stream()
                .filter(msg -> "ACCEPTED".equalsIgnoreCase(msg.challengeDecision()))
                .count();

        long rejected = messages.stream()
                .filter(msg -> "REJECTED".equalsIgnoreCase(msg.challengeDecision()))
                .count();

        return new ChatPersonalizationContext.ChallengeHistoryEntry(originalTitle, (int) accepted, (int) rejected);
    }

    // Arma el system prompt enriquecido que se le envía al LLM.
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

    // Trae el nombre registrado y las preferencias de conversación del usuario.
    private ChatPersonalizationContext loadPersonalizationContext(Long userId, List<ChatPersonalizationContext.ChallengeHistoryEntry> challengeHistory) {
        String registeredName = userRepository.findById(userId)
                .map(AppUser::getName)
                .orElse(null);
        ChatConversationPreference preference =
                chatConversationPreferenceRepository.findByUserId(userId).orElse(null);
        return ChatPersonalizationContext.from(registeredName, preference, challengeHistory);
    }

    // Prompt base configurado (cadena vacía si no hay configuración).
    private String basePrompt() {
        return chatConfigRepository.findFirst()
                .map(ChatConfig::getSystemPrompt)
                .orElse("");
    }

    // Agrega la pregunta de estilo de comunicación si la respuesta es segura y quedó pendiente.
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

    // Aplica el resultado de la recomendación a la reply y persiste la memoria correspondiente.
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

    // Persiste el reto generado en la memoria vectorial si es recordable.
    private void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        if (challenge == null || !challenge.isRememberable()) {
            return;
        }
        userVectorMemoryService.saveMemory(
                mapper.toGeneratedChallengeCommand(userId, conversationId, challenge));
    }

    // Persiste en la memoria vectorial la actividad recomendada.
    private void rememberRecommendedActivity(Long userId, String conversationId, Long emotionalEventId, SuggestedChatAction action) {
        if (action == null) {
            return;
        }
        userVectorMemoryService.saveMemory(
                mapper.toRecommendedActivityCommand(userId, conversationId, emotionalEventId, action));
    }

    // Garantiza un reto por defecto cuando el usuario lo pidió y no se generó ninguno.
    private ChatReply ensureRequestedChallenge(
            ChatReply reply,
            ChatUserIntent userIntent,
            SuggestedChatAction suggestedAction) {
        if (userIntent != ChatUserIntent.CHALLENGE_REQUEST
                || suggestedAction != null
                || reply.generatedChallenge() != null) {
            return reply;
        }
        return reply.withRequestedActionChallenge();
    }

    // Adjunta a la reply la emoción e intensidad detectadas cuando son aprovechables.
    private ChatReply applyAnalysisMetadata(ChatReply reply, EmotionalAnalysisResult analysis) {
        if (analysis == null || !analysis.hasUsableEmotion()) {
            return reply;
        }
        return reply.withEmotionalMetadata(analysis.detectedEmotion(), analysis.chatIntensity());
    }

    // Registra el turno completo: historial de conversación + memoria del usuario.
    private void recordConversationTurn(String conversationId, String message, ChatReply reply, Long userId) {
        saveConversationExchange(conversationId, message, reply, userId);
        rememberChatMessage(userId, conversationId, message);
    }

    // Guarda en el historial tanto el mensaje del usuario como el del asistente.
    private void saveConversationExchange(String conversationId, String message, ChatReply reply, Long userId) {
        saveUserMessage(conversationId, message, reply, userId);
        saveAssistantMessage(conversationId, reply, userId);
    }

    // Guarda el mensaje del usuario en el historial (tolerante a errores).
    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, mapper.toUserMessage(message, reply), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    // Guarda el mensaje del asistente en el historial (tolerante a errores).
    private void saveAssistantMessage(String conversationId, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, mapper.toAssistantMessage(reply), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje del asistente userId={} conversationId={}", userId, conversationId, e);
        }
    }

    // Contexto inmutable que se acumula durante el procesamiento del mensaje.
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
