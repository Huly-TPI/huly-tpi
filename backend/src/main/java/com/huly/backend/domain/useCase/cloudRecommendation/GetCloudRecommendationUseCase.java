package com.huly.backend.domain.useCase.cloudRecommendation;

import com.huly.backend.domain.model.CloudRecommendation;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GetCloudRecommendationUseCase {

    private final org.springframework.core.io.Resource cloudAnalysisPrompt;

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final ChatEmotionalRecommendationPolicy recommendationPolicy;
    private final GetEmotionalRecommendationsUseCase recommendationsUseCase;

    public GetCloudRecommendationUseCase(
            @Value("classpath:/prompts/cloud-analysis.st") org.springframework.core.io.Resource cloudAnalysisPrompt,
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            GetEmotionalRecommendationsUseCase recommendationsUseCase) {
        this.cloudAnalysisPrompt = cloudAnalysisPrompt;
        this.emotionalAnalysisPort = emotionalAnalysisPort;
        this.promptBuilderService = promptBuilderService;
        this.recommendationPolicy = recommendationPolicy;
        this.recommendationsUseCase = recommendationsUseCase;
    }

    public CloudRecommendation execute(List<String> thoughts) {
        return execute(thoughts, null);
    }

    public CloudRecommendation execute(List<String> thoughts, Long userId) {
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

            return toCloudRecommendation(result.recommendations().get(0));
        } catch (Exception e) {
            log.warn("Error al procesar recomendación, usando fallback.", e);
            return fallback();
        }
    }

    private EmotionalAnalysisResult analyze(String userMessage) {
        String promptText = "";
        if (cloudAnalysisPrompt != null) {
            try {
                promptText = cloudAnalysisPrompt.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                log.warn("Error leyendo prompt de nube", e);
            }
        }
        String prompt = promptBuilderService.buildEmotionalAnalysisPrompt(promptText, List.of());
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

    private CloudRecommendation toCloudRecommendation(EmotionalRecommendationItem recommendation) {
        ActivityType type = recommendation.type();
        String activityType = toPublicActivityType(type);
        return new CloudRecommendation(
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
            return "clouds";
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
            return "/clouds";
        }
        if (type == ActivityType.BURBUJA) {
            return "/bubbles";
        }
        return "/diary";
    }

    private CloudRecommendation fallback() {
        return new CloudRecommendation(
                "diary",
                "diary",
                "Escribí en tu diario",
                "Plasmar lo que sentiste puede ayudarte a procesarlo con más profundidad.",
                "/diary"
        );
    }
}
