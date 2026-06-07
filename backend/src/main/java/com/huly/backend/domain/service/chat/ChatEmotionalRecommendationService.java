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
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates emotional analysis, recommendation ranking and event persistence for chatbot messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEmotionalRecommendationService {

    private static final String ACTIVITIES_URL = "/api/activities";

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final ChatEmotionalRecommendationPolicy recommendationPolicy;
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

        EmotionalAnalysisResult recommendationAnalysis = recommendationPolicy.resolve(
                userId,
                analysis,
                conversationalReply,
                forceRecommendation
        );
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
                    analysis.vad(),
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
