package com.huly.backend.domain.useCase.lanternRecommendation;

import com.huly.backend.domain.model.cloudRecommendation.LanternRecommendation;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendation;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalRecommendationResult;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.emotionalRecommendation.EmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetLanternRecommendationUseCase {

    private static final int HISTORY_LIMIT = 20;

    private static final String CLOUD_ANALYSIS_CONTEXT = """
            Eres Huly, un asistente de bienestar mental.
            El usuario acaba de completar el ejercicio de nubes emocionales y escribio pensamientos o emociones que queria soltar.
            Analiza esos pensamientos para decidir una actividad de bienestar con el motor comun de recomendaciones.
            """;

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final ChatEmotionalRecommendationPolicy recommendationPolicy;
    private final EmotionalRecommendationService recommendationService;
    private final ActivityRepository activityRepository;
    private final EmotionalEventRepository emotionalEventRepository;

    public LanternRecommendation execute(List<String> thoughts) {
        return execute(thoughts, null);
    }

    public LanternRecommendation execute(List<String> thoughts, Long userId) {
        String userMessage = String.join("\n", thoughts);
        try {
            EmotionalAnalysisResult analysis = analyze(userMessage);
            EmotionalAnalysisResult recommendationAnalysis = recommendationPolicy.resolve(
                    userId,
                    analysis,
                    null,
                    true
            );

            EmotionalRecommendation query = toQuery(recommendationAnalysis, userId);
            EmotionalRecommendationResult result = recommendationService.recommend(
                    query,
                    activities(),
                    userHistory(userId)
            );
            if (result.recommendations().isEmpty()) {
                return fallback();
            }

            return toLanternRecommendation(result.recommendations().get(0));
        } catch (Exception e) {
            log.warn("Error al procesar recomendación, usando fallback.", e);
            return fallback();
        }
    }

    private EmotionalAnalysisResult analyze(String userMessage) {
        String prompt = promptBuilderService.buildEmotionalAnalysisPrompt(CLOUD_ANALYSIS_CONTEXT, List.of());
        EmotionalAnalysisResult result = emotionalAnalysisPort.analyze(prompt, userMessage, List.of());
        return result == null ? EmotionalAnalysisResult.neutral() : result;
    }

    private EmotionalRecommendation toQuery(EmotionalAnalysisResult analysis, Long userId) {
        return new EmotionalRecommendation(
                userId,
                analysis.vad(),
                analysis.intensity(),
                analysis.userGoal()
        );
    }

    private List<Activity> activities() {
        return activityRepository.findAll();
    }

    private List<EmotionalEvent> userHistory(Long userId) {
        if (userId == null)
            return List.of();

        return emotionalEventRepository.findRecentRecommendationHistoryByUserId(userId, HISTORY_LIMIT);
    }

    private LanternRecommendation toLanternRecommendation(EmotionalRecommendationItem recommendation) {
        ActivityType type = recommendation.type();
        String activityType = toPublicActivityType(type);
        return new LanternRecommendation(
                activityType,
                activityType,
                recommendation.title(),
                recommendation.description(),
                redirectUrl(type)
        );
    }

    private String toPublicActivityType(ActivityType type) {
        if (type == ActivityType.RESPIRACION) {
            return "breathing";
        }
        if (type == ActivityType.NUBE) {
            return "lanterns";
        }
        if (type == ActivityType.BURBUJA) {
            return "bubbles";
        }
        return "diary";
    }

    private String redirectUrl(ActivityType type) {
        if (type == ActivityType.RESPIRACION) {
            return "/guided-breathing";
        }
        if (type == ActivityType.NUBE) {
            return "/lanterns";
        }
        if (type == ActivityType.BURBUJA) {
            return "/bubbles";
        }
        return "/diary";
    }

    private LanternRecommendation fallback() {
        return new LanternRecommendation(
                "diary",
                "diary",
                "Escribí en tu diario",
                "Plasmar lo que sentiste puede ayudarte a procesarlo con más profundidad.",
                "/diary"
        );
    }
}
