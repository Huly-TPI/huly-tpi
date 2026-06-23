package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.chat.*;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
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

import java.text.Normalizer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class ChatUseCase {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9ñ ]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final List<String> REQUEST_MARKERS = List.of(
            "quiero", "quisiera", "necesito", "dame", "dame una", "dame un", "mandame", "mostrame",
            "proponeme", "proponerme", "sugerime", "sugiereme", "recomendame", "recomiendame",
            "me recomendas", "me recomiendas", "me sugeris", "me sugieres", "me das", "me propones",
            "podrias darme", "podrias proponerme", "podrias recomendarme", "podes darme",
            "podes proponerme", "puedes darme", "puedes proponerme", "que puedo hacer", "algo para hacer"
    );

    private static final List<String> CHALLENGE_TERMS = List.of(
            "reto", "retos", "desafio", "desafios", "challenge"
    );

    private static final List<String> ACTIVITY_TERMS = List.of(
            "actividad", "actividades", "ejercicio", "ejercicios", "practica", "practicas",
            "recomendacion", "recomendaciones", "sugerencia", "sugerencias",
            "algo para sentirme mejor", "algo que me ayude"
    );

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static final String RECOMMENDED_ACTIVITY = "RECOMMENDED_ACTIVITY";
    private static final String GENERATED_CHALLENGE = "GENERATED_CHALLENGE";

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

    public ChatReply execute(String message, String conversationId, Long userId) {
        ChatPreferenceHandlingResult preferenceResult =
                handleChatPreferencesUseCase.execute(userId, conversationId, message);
        if (!preferenceResult.continueConversation()) {
            return preferenceResult.directReply();
        }

        ChatUserIntent userIntent = detectIntent(message);

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
        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                userId != null ? userId.toString() : null,
                USER_CHAT_MESSAGE,
                "CHAT_MESSAGE",
                message,
                conversationId,
                null,
                Map.of("createdFrom", CREATED_FROM_USER_MESSAGE, "feature", "CHATBOT")
        ));
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
        if (!offerCommunicationStyleWhenSafe
                || Boolean.TRUE.equals(reply.riskDetected())
                || (reply.intensity() != null && reply.intensity() >= 7)
                || reply.suggestedAction() != null) {
            return reply;
        }

        ChatConversationPreference pendingPreference = chatConversationPreferenceRepository.findByUserId(userId)
                .filter(preference -> preference.getOnboardingStatus()
                        == ChatOnboardingStatus.PENDING_COMMUNICATION_STYLE)
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
            rememberGeneratedChallenge(userId, conversationId, enriched.generatedChallenge());
            return enriched;
        }

        rememberRecommendedActivity(userId, conversationId, outcome.suggestedAction().emotionalEventId(), outcome.suggestedAction());

        return new ChatReply(
                enriched.content(),
                enriched.detectedEmotion(),
                enriched.intensity(),
                enriched.riskDetected(),
                enriched.matchedWord(),
                outcome.suggestedAction(),
                null);
    }

    private void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        if (challenge == null || challenge.title() == null || challenge.title().isBlank()) {
            return;
        }
        String content = "Huly sugirio el reto: %s. Descripcion: %s."
                .formatted(challenge.title(), challenge.description() == null ? "" : challenge.description());
        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                String.join(":", "generated-challenge", conversationId != null && !conversationId.isBlank() ? conversationId.strip() : "unknown", challenge.title().strip()),
                GENERATED_CHALLENGE,
                "GENERATED_CHALLENGE",
                content,
                conversationId,
                null,
                Map.of("createdFrom", CREATED_FROM_USER_MESSAGE, "feature", "CHATBOT_CHALLENGE",
                        "challengeTitle", challenge.title(),
                        "challengeDescription", challenge.description() == null ? "" : challenge.description())
        ));
    }

    private void rememberRecommendedActivity(Long userId, String conversationId, Long emotionalEventId, SuggestedChatAction action) {
        if (action == null) {
            return;
        }
        String content = "Huly recomendo la actividad: %s. Tipo: %s. Descripcion: %s."
                .formatted(
                        action.title() == null ? "Actividad" : action.title(),
                        action.type() != null ? action.type().name() : "UNKNOWN",
                        action.description() == null ? "" : action.description()
                );
        Map<String, Object> extra = new HashMap<>();
        extra.put("createdFrom", CREATED_FROM_USER_MESSAGE);
        extra.put("feature", "CHATBOT_ACTIVITY_RECOMMENDATION");
        extra.put("activityId", action.activityId() == null ? "" : action.activityId().toString());
        extra.put("activityType", action.type() != null ? action.type().name() : "");
        extra.put("emotionalEventId", emotionalEventId != null ? emotionalEventId.toString() : "");

        userVectorMemoryService.saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                emotionalEventId != null ? emotionalEventId.toString() : (userId != null ? userId.toString() : null),
                RECOMMENDED_ACTIVITY,
                "RECOMMENDED_ACTIVITY",
                content,
                conversationId,
                emotionalEventId != null ? emotionalEventId.toString() : null,
                extra
        ));
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

    private void saveConversationExchange(String conversationId, String message, ChatReply reply, Long userId) {
        saveUserMessage(conversationId, message, reply, userId);
        saveAssistantMessage(conversationId, reply, userId);
    }

    private void saveUserMessage(String conversationId, String message, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                    MessageRole.USER,
                    message,
                    reply.detectedEmotion(),
                    reply.riskDetected(),
                    reply.matchedWord(),
                    null,
                    null,
                    null,
                    null), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje de usuario userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private void saveAssistantMessage(String conversationId, ChatReply reply, Long userId) {
        try {
            chatMemoryPort.addMessage(conversationId, new ConversationMessage(
                    MessageRole.ASSISTANT,
                    reply.content(),
                    null,
                    null,
                    null,
                    reply.suggestedAction(),
                    reply.generatedChallenge(),
                    null,
                    null), userId);
        } catch (Exception e) {
            log.warn("No se pudo guardar mensaje del asistente userId={} conversationId={}", userId, conversationId, e);
        }
    }

    private ChatUserIntent detectIntent(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank()) {
            return ChatUserIntent.NONE;
        }

        if (isExplicitChallengeRequest(normalized)) {
            return ChatUserIntent.CHALLENGE_REQUEST;
        }
        if (isExplicitActivityRecommendationRequest(normalized)) {
            return ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST;
        }
        return ChatUserIntent.NONE;
    }

    private boolean isExplicitChallengeRequest(String message) {
        return containsAny(message, CHALLENGE_TERMS) && containsAny(message, REQUEST_MARKERS);
    }

    private boolean isExplicitActivityRecommendationRequest(String message) {
        if (containsAny(message, ACTIVITY_TERMS) && containsAny(message, REQUEST_MARKERS)) {
            return true;
        }
        return containsAny(message, List.of(
                "recomendame algo", "recomiendame algo", "sugerime algo", "sugiereme algo",
                "que puedo hacer para sentirme mejor", "necesito algo para sentirme mejor",
                "algo para bajar la ansiedad", "algo para calmarme", "algo que me ayude a calmarme"
        ));
    }

    private boolean containsAny(String message, List<String> terms) {
        return terms.stream().anyMatch(message::contains);
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }
        String withoutDiacritics = DIACRITICS.matcher(Normalizer.normalize(message, Normalizer.Form.NFD))
                .replaceAll("");
        String lower = withoutDiacritics.toLowerCase();
        String wordsOnly = NON_WORD.matcher(lower).replaceAll(" ");
        return MULTI_SPACE.matcher(wordsOnly).replaceAll(" ").trim();
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
