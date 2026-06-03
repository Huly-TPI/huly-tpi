package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.chat.ChatRecommendationOutcome;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
import com.huly.backend.domain.useCase.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.GetEmotionalRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEmotionalRecommendationService {

    private static final String ACTIVITIES_URL = "/api/activities";
    private static final int CHAT_INTENSITY_THRESHOLD = 7;
    private static final double ANALYSIS_INTENSITY_THRESHOLD = 0.65;
    private static final double LOW_VALENCE_THRESHOLD = -0.45;
    private static final double LOW_DOMINANCE_THRESHOLD = -0.45;
    private static final double FALLBACK_CONFIDENCE = 0.80;
    private static final Set<EmotionType> HIGH_DISTRESS_EMOTIONS = EnumSet.of(
            EmotionType.GRIEF,
            EmotionType.SADNESS,
            EmotionType.ANXIETY,
            EmotionType.STRESS,
            EmotionType.OVERWHELM,
            EmotionType.PANIC,
            EmotionType.HOPELESSNESS,
            EmotionType.EMPTINESS,
            EmotionType.LONELINESS,
            EmotionType.NUMBNESS,
            EmotionType.EXHAUSTION
    );

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final GetEmotionalRecommendationsUseCase recommendationsUseCase;
    private final CreateEmotionalEventUseCase createEmotionalEventUseCase;

    public ChatRecommendationOutcome evaluate(
            String message,
            Long userId,
            String basePrompt,
            List<VectorMemory> memories,
            List<ConversationMessage> history
    ) {
        return evaluate(message, userId, basePrompt, memories, history, null);
    }

    public ChatRecommendationOutcome evaluate(
            String message,
            Long userId,
            String basePrompt,
            List<VectorMemory> memories,
            List<ConversationMessage> history,
            ChatReply conversationalReply
    ) {
        return evaluate(message, userId, basePrompt, memories, history, conversationalReply, false);
    }

    public ChatRecommendationOutcome evaluate(
            String message,
            Long userId,
            String basePrompt,
            List<VectorMemory> memories,
            List<ConversationMessage> history,
            ChatReply conversationalReply,
            boolean forceRecommendation
    ) {
        EmotionalAnalysisResult analysis = analyze(message, basePrompt, memories, history);
        logAnalysisResult(userId, analysis);

        EmotionalAnalysisResult recommendationAnalysis = resolveRecommendationAnalysis(
                userId,
                analysis,
                conversationalReply
        );
        if (forceRecommendation && !recommendationAnalysis.shouldRecommend()) {
            log.info("emotional_recommendation_override userId={} reason=explicit_activity_request", userId);
            recommendationAnalysis = forceRecommendationFromExplicitRequest(recommendationAnalysis);
        }
        if (!recommendationAnalysis.shouldRecommend()) {
            return ChatRecommendationOutcome.none(recommendationAnalysis);
        }

        return recommendAndPersistEvent(message, userId, recommendationAnalysis);
    }

    private EmotionalAnalysisResult analyze(
            String message,
            String basePrompt,
            List<VectorMemory> memories,
            List<ConversationMessage> history
    ) {
        try {
            String analysisPrompt = promptBuilderService.buildEmotionalAnalysisPrompt(basePrompt, memories);
            EmotionalAnalysisResult result = emotionalAnalysisPort.analyze(analysisPrompt, message, history);
            return result == null ? EmotionalAnalysisResult.neutral() : result;
        } catch (Exception e) {
            log.warn("No se pudo completar el analisis emocional estructurado", e);
            return EmotionalAnalysisResult.neutral();
        }
    }

    private ChatRecommendationOutcome recommendAndPersistEvent(
            String message,
            Long userId,
            EmotionalAnalysisResult analysis
    ) {
        try {
            EmotionalRecommendationQuery query = new EmotionalRecommendationQuery(
                    userId,
                    EmotionalEventSource.CHATBOT,
                    message,
                    emotionName(analysis),
                    analysis.confidence(),
                    analysis.valence(),
                    analysis.arousal(),
                    analysis.dominance(),
                    analysis.intensity(),
                    analysis.userGoal()
            );
            EmotionalRecommendationResult result = recommendationsUseCase.execute(query);
            if (result.recommendations().isEmpty()) {
                log.warn("Analisis solicito recomendacion pero no hay actividades disponibles userId={}", userId);
                return ChatRecommendationOutcome.none(analysis);
            }

            EmotionalRecommendationItem recommendation = result.recommendations().get(0);
            EmotionalEvent event = createEmotionalEventUseCase.execute(toEventCommand(
                    message,
                    userId,
                    analysis,
                    recommendation
            ));
            log.info("emotional_recommendation_created userId={} eventId={} activityId={} type={}",
                    userId, event.getId(), recommendation.activityId(), recommendation.type());
            return new ChatRecommendationOutcome(analysis, toSuggestedAction(recommendation, event));
        } catch (Exception e) {
            log.warn("emotional_recommendation_failed userId={} reason={}", userId, e.getMessage(), e);
            return ChatRecommendationOutcome.none(analysis);
        }
    }

    private EmotionalAnalysisResult resolveRecommendationAnalysis(
            Long userId,
            EmotionalAnalysisResult analysis,
            ChatReply conversationalReply
    ) {
        if (analysis == null) {
            return shouldOverrideFromConversation(conversationalReply)
                    ? fallbackFromConversation(conversationalReply)
                    : EmotionalAnalysisResult.neutral();
        }

        if (analysis.shouldRecommend()) {
            return analysis;
        }

        if (shouldOverrideFromAnalysis(analysis)) {
            log.info("emotional_recommendation_override userId={} emotion={} intensity={} reason=structured_high_distress",
                    userId, analysis.detectedEmotion(), analysis.intensity());
            return forceRecommendation(analysis);
        }

        if (shouldOverrideFromConversation(conversationalReply)) {
            log.info("emotional_recommendation_override userId={} emotion={} intensity={} reason=conversation_metadata_high_distress",
                    userId, conversationalReply.detectedEmotion(), conversationalReply.intensity());
            return fallbackFromConversation(conversationalReply);
        }

        return analysis;
    }

    private boolean shouldOverrideFromAnalysis(EmotionalAnalysisResult analysis) {
        if (analysis == null || analysis.detectedEmotion() == null) {
            return false;
        }
        return HIGH_DISTRESS_EMOTIONS.contains(analysis.detectedEmotion())
                && (analysis.intensity() >= ANALYSIS_INTENSITY_THRESHOLD
                || analysis.valence() <= LOW_VALENCE_THRESHOLD
                || analysis.dominance() <= LOW_DOMINANCE_THRESHOLD);
    }

    private boolean shouldOverrideFromConversation(ChatReply reply) {
        return reply != null
                && reply.detectedEmotion() != null
                && reply.intensity() != null
                && HIGH_DISTRESS_EMOTIONS.contains(reply.detectedEmotion())
                && reply.intensity() >= CHAT_INTENSITY_THRESHOLD;
    }

    private EmotionalAnalysisResult forceRecommendation(EmotionalAnalysisResult analysis) {
        return new EmotionalAnalysisResult(
                true,
                analysis.detectedEmotion(),
                Math.max(analysis.confidence(), FALLBACK_CONFIDENCE),
                analysis.valence(),
                analysis.arousal(),
                analysis.dominance(),
                Math.max(analysis.intensity(), ANALYSIS_INTENSITY_THRESHOLD),
                valueOrDefault(analysis.userGoal(), defaultUserGoal(analysis.detectedEmotion())),
                valueOrDefault(analysis.shortReason(), "Malestar emocional significativo detectado.")
        );
    }

    private EmotionalAnalysisResult forceRecommendationFromExplicitRequest(EmotionalAnalysisResult analysis) {
        EmotionalAnalysisResult safeAnalysis = analysis != null ? analysis : EmotionalAnalysisResult.neutral();
        return new EmotionalAnalysisResult(
                true,
                safeAnalysis.detectedEmotion() != null ? safeAnalysis.detectedEmotion() : EmotionType.NEUTRAL,
                Math.max(safeAnalysis.confidence(), 0.70),
                safeAnalysis.valence(),
                safeAnalysis.arousal(),
                safeAnalysis.dominance(),
                Math.max(safeAnalysis.intensity(), 0.35),
                valueOrDefault(safeAnalysis.userGoal(), "recibir una actividad de bienestar"),
                "El usuario pidio explicitamente una recomendacion de actividad."
        );
    }

    private EmotionalAnalysisResult fallbackFromConversation(ChatReply reply) {
        return switch (reply.detectedEmotion()) {
            case GRIEF, SADNESS -> fallbackAnalysis(reply, -0.85, 0.35, -0.75, 0.85,
                    "procesar duelo o tristeza y sentirse acompanado");
            case ANXIETY, PANIC -> fallbackAnalysis(reply, -0.75, 0.85, -0.70, 0.90,
                    "calmarse y bajar la ansiedad");
            case STRESS, OVERWHELM -> fallbackAnalysis(reply, -0.65, 0.75, -0.65, 0.80,
                    "regular estres y recuperar control");
            case HOPELESSNESS, EMPTINESS, LONELINESS -> fallbackAnalysis(reply, -0.90, 0.25, -0.80, 0.85,
                    "sentirse acompanado y aliviar tristeza profunda");
            default -> fallbackAnalysis(reply, -0.60, 0.50, -0.60, 0.75,
                    defaultUserGoal(reply.detectedEmotion()));
        };
    }

    private EmotionalAnalysisResult fallbackAnalysis(
            ChatReply reply,
            double valence,
            double arousal,
            double dominance,
            double intensity,
            String userGoal
    ) {
        return new EmotionalAnalysisResult(
                true,
                reply.detectedEmotion(),
                FALLBACK_CONFIDENCE,
                valence,
                arousal,
                dominance,
                intensity,
                userGoal,
                "La metadata conversacional detecto malestar emocional significativo."
        );
    }

    private String defaultUserGoal(EmotionType emotion) {
        if (emotion == null) {
            return "regular el estado emocional";
        }
        return switch (emotion) {
            case GRIEF, SADNESS -> "procesar tristeza y sentirse acompanado";
            case ANXIETY, PANIC -> "calmarse y bajar la ansiedad";
            case STRESS, OVERWHELM -> "regular estres y recuperar control";
            case HOPELESSNESS, EMPTINESS, LONELINESS -> "sentirse acompanado y aliviar tristeza profunda";
            default -> "regular el estado emocional";
        };
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void logAnalysisResult(Long userId, EmotionalAnalysisResult analysis) {
        if (analysis == null) {
            log.info("emotional_analysis_result userId={} result=null", userId);
            return;
        }
        log.info(
                "emotional_analysis_result userId={} shouldRecommend={} emotion={} confidence={} valence={} arousal={} dominance={} intensity={} reason={}",
                userId,
                analysis.shouldRecommend(),
                analysis.detectedEmotion(),
                analysis.confidence(),
                analysis.valence(),
                analysis.arousal(),
                analysis.dominance(),
                analysis.intensity(),
                analysis.shortReason()
        );
    }

    private CreateEmotionalEventCommand toEventCommand(
            String message,
            Long userId,
            EmotionalAnalysisResult analysis,
            EmotionalRecommendationItem recommendation
    ) {
        return new CreateEmotionalEventCommand(
                userId,
                EmotionalEventSource.CHATBOT,
                message,
                emotionName(analysis),
                analysis.confidence(),
                analysis.valence(),
                analysis.arousal(),
                analysis.dominance(),
                analysis.intensity(),
                analysis.userGoal(),
                generatedRecommendationText(recommendation),
                recommendation.activityId(),
                null
        );
    }

    private SuggestedChatAction toSuggestedAction(EmotionalRecommendationItem recommendation, EmotionalEvent event) {
        return new SuggestedChatAction(
                recommendation.type(),
                recommendation.activityId(),
                recommendation.title(),
                recommendation.description(),
                ACTIVITIES_URL,
                event.getId()
        );
    }

    private String generatedRecommendationText(EmotionalRecommendationItem recommendation) {
        String reason = recommendation.reason() == null || recommendation.reason().isBlank()
                ? ""
                : " " + recommendation.reason();
        return recommendation.title() + ": " + recommendation.description() + reason;
    }

    private String emotionName(EmotionalAnalysisResult analysis) {
        EmotionType emotion = analysis.detectedEmotion() == null ? EmotionType.NEUTRAL : analysis.detectedEmotion();
        return emotion.name();
    }
}
