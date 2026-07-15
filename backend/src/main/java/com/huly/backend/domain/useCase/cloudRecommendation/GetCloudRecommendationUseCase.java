package com.huly.backend.domain.useCase.cloudRecommendation;

import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationRequest;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.domain.mapper.cloudRecommendation.GetCloudRecommendationMapper;
import com.huly.backend.domain.model.cloudRecommendation.CloudRecommendation;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GetCloudRecommendationUseCase {
    private static final int HISTORY_LIMIT = 20;

    private final org.springframework.core.io.Resource cloudAnalysisPrompt;

    private final EmotionalAnalysisPort emotionalAnalysisPort;
    private final PromptBuilderService promptBuilderService;
    private final ChatEmotionalRecommendationPolicy recommendationPolicy;
    private final EmotionalRecommendationService recommendationService;
    private final ActivityRepository activityRepository;
    private final EmotionalEventRepository emotionalEventRepository;
    private final GetCloudRecommendationMapper mapper;

    public GetCloudRecommendationUseCase(
            @Value("classpath:/prompts/cloud-analysis.st") org.springframework.core.io.Resource cloudAnalysisPrompt,
            EmotionalAnalysisPort emotionalAnalysisPort,
            PromptBuilderService promptBuilderService,
            ChatEmotionalRecommendationPolicy recommendationPolicy,
            EmotionalRecommendationService recommendationService,
            ActivityRepository activityRepository,
            EmotionalEventRepository emotionalEventRepository,
            GetCloudRecommendationMapper mapper) {
        this.cloudAnalysisPrompt = cloudAnalysisPrompt;
        this.emotionalAnalysisPort = emotionalAnalysisPort;
        this.promptBuilderService = promptBuilderService;
        this.recommendationPolicy = recommendationPolicy;
        this.recommendationService = recommendationService;
        this.activityRepository = activityRepository;
        this.emotionalEventRepository = emotionalEventRepository;
        this.mapper = mapper;
    }

    public GetCloudRecommendationResponse execute(GetCloudRecommendationRequest request) {
        List<String> thoughts = request.thoughts();
        Long userId = request.userId();
        String userMessage = String.join("\n", thoughts);
        try {
            EmotionalAnalysisResult analysis = analyze(userMessage);
            EmotionalAnalysisResult recommendationAnalysis = recommendationPolicy.resolve(
                    userId,
                    analysis,
                    null,
                    true
            );

            EmotionalRecommendation query = toQuery(recommendationAnalysis, userId, userMessage);
            log.info("cloud_recommendation_query userId={} userGoal='{}' valence={} arousal={} dominance={} intensity={}",
                    userId, query.userGoal(), query.vad().valence(), query.vad().arousal(),
                    query.vad().dominance(), query.intensity());
            List<Activity> filteredActivities = activities().stream()
                    .filter(activity -> activity.getType() != ActivityType.LANTERN)
                    .collect(Collectors.toList());

            EmotionalRecommendationResult result = recommendationService.recommend(
                    query,
                    filteredActivities,
                    userHistory(userId)
            );
            if (result.recommendations().isEmpty()) {
                return mapper.toResponse(fallback());
            }

            return mapper.toResponse(toCloudRecommendation(result.recommendations().get(0)));
        } catch (Exception e) {
            log.warn("Error al procesar recomendación, usando fallback.", e);
            return mapper.toResponse(fallback());
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

    private EmotionalRecommendation toQuery(EmotionalAnalysisResult analysis, Long userId, String userMessage) {
        return new EmotionalRecommendation(
                userId,
                analysis.vad(),
                analysis.intensity(),
                combineGoalSignal(analysis.userGoal(), userMessage)
        );
    }

    private String combineGoalSignal(String analysisGoal, String userMessage) {
        if (analysisGoal == null || analysisGoal.isBlank()) {
            return userMessage;
        }
        return analysisGoal + " " + userMessage;
    }

    private List<Activity> activities() {
        return activityRepository.findAll();
    }

    private List<EmotionalEvent> userHistory(Long userId) {
        if (userId == null)
            return List.of();

        return emotionalEventRepository.findRecentRecommendationHistoryByUserId(userId, HISTORY_LIMIT);
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
        if (type == null) {
            return "diary";
        }
        return switch (type) {
            case BREATHING -> "breathing";
            case LANTERN -> "lanterns";
            case BUBBLE -> "bubbles";
            case CHALLENGE -> "challenge";
            case ZEN_GARDEN -> "zen_garden";
            case MANDALA -> "mandala";
            case STONES -> "stones";
            case PENDING -> "pending";
            case DIARY -> "diary";
        };
    }

    private String redirectUrl(ActivityType type) {
        if (type == null) {
            return "/diary";
        }
        return switch (type) {
            case BREATHING -> "/guided-breathing";
            case LANTERN -> "/lanterns";
            case BUBBLE -> "/bubbles";
            case CHALLENGE -> "/challenges";
            case ZEN_GARDEN -> "/zen-sand-garden";
            case MANDALA -> "/mandalas";
            case STONES -> "/stones";
            case PENDING -> "/pending";
            case DIARY -> "/diary";
        };
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
