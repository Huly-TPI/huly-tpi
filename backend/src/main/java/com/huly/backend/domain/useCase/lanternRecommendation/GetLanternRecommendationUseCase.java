package com.huly.backend.domain.useCase.lanternRecommendation;

import com.huly.backend.domain.model.LanternRecommendation;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.provider.EmotionalAnalysisPort;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetLanternRecommendationUseCase {

    private static final String LANTERN_ANALYSIS_CONTEXT = """
            Eres Huly, un asistente de bienestar mental.
            El usuario acaba de completar el ejercicio de faroles y escribio pensamientos o emociones que queria soltar.
            Analiza esos pensamientos para decidir una actividad de bienestar con el motor comun de recomendaciones.
            """;

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final ChatEmotionalRecommendationPolicy recommendationPolicy;
    private final GetEmotionalRecommendationsUseCase recommendationsUseCase;

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

            EmotionalRecommendationResult result = recommendationsUseCase.execute(toQuery(recommendationAnalysis, userId));
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
        String prompt = promptBuilderService.buildEmotionalAnalysisPrompt(LANTERN_ANALYSIS_CONTEXT, List.of());
        EmotionalAnalysisResult result = emotionalAnalysisPort.analyze(prompt, userMessage, List.of());
        return result == null ? EmotionalAnalysisResult.neutral() : result;
    }

    private EmotionalRecommendationQuery toQuery(EmotionalAnalysisResult analysis, Long userId) {
        return new EmotionalRecommendationQuery(
                userId,
                analysis.vad(),
                analysis.intensity(),
                analysis.userGoal()
        );
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
