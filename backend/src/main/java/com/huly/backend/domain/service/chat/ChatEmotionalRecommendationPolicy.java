package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Applies chatbot-specific rules that decide when an emotional recommendation is required.
 */
@Slf4j
@Service
public class ChatEmotionalRecommendationPolicy {

    private static final int CHAT_INTENSITY_THRESHOLD = 7;
    private static final double ANALYSIS_INTENSITY_THRESHOLD = 0.65;
    private static final double LOW_VALENCE_THRESHOLD = -0.45;
    private static final double LOW_DOMINANCE_THRESHOLD = -0.45;
    private static final double FALLBACK_CONFIDENCE = 0.80;
    private static final double EXPLICIT_REQUEST_CONFIDENCE = 0.70;
    private static final double EXPLICIT_REQUEST_INTENSITY = 0.35;
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

    /**
     * Resolves the analysis that should drive recommendation ranking.
     *
     * @param userId user identifier used for diagnostic logging
     * @param analysis structured emotional analysis
     * @param conversationalReply optional conversational metadata fallback
     * @param forceRecommendation whether the user explicitly requested an activity
     * @return resolved analysis, possibly enriched with a fallback VAD profile
     */
    public EmotionalAnalysisResult resolve(
            Long userId,
            EmotionalAnalysisResult analysis,
            ChatReply conversationalReply,
            boolean forceRecommendation
    ) {
        EmotionalAnalysisResult resolved = resolveDistressAnalysis(userId, analysis, conversationalReply);
        if (forceRecommendation && !resolved.shouldRecommend()) {
            log.info("emotional_recommendation_override userId={} reason=explicit_activity_request", userId);
            return forceFromExplicitRequest(resolved);
        }
        return resolved;
    }

    private EmotionalAnalysisResult resolveDistressAnalysis(
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
        return analysis.detectedEmotion() != null
                && HIGH_DISTRESS_EMOTIONS.contains(analysis.detectedEmotion())
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

    private EmotionalAnalysisResult forceFromExplicitRequest(EmotionalAnalysisResult analysis) {
        EmotionalAnalysisResult safeAnalysis = analysis != null ? analysis : EmotionalAnalysisResult.neutral();
        return new EmotionalAnalysisResult(
                true,
                safeAnalysis.detectedEmotion() != null ? safeAnalysis.detectedEmotion() : EmotionType.NEUTRAL,
                Math.max(safeAnalysis.confidence(), EXPLICIT_REQUEST_CONFIDENCE),
                safeAnalysis.valence(),
                safeAnalysis.arousal(),
                safeAnalysis.dominance(),
                Math.max(safeAnalysis.intensity(), EXPLICIT_REQUEST_INTENSITY),
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
}
