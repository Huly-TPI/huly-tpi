package com.huly.backend.domain.useCase.cloudsRecommendation;

import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetCloudRecommendationUseCaseTest {

    private CapturingEmotionalAnalysisPort emotionalAnalysisPort;
    private CapturingRecommendationsUseCase recommendationsUseCase;
    private GetCloudRecommendationUseCase useCase;

    @BeforeEach
    void setUp() {
        emotionalAnalysisPort = new CapturingEmotionalAnalysisPort();
        recommendationsUseCase = new CapturingRecommendationsUseCase();
        useCase = new GetCloudRecommendationUseCase(new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()), 
                emotionalAnalysisPort,
                new PromptBuilderService(),
                new ChatEmotionalRecommendationPolicy(),
                recommendationsUseCase
        );
    }

    @Test
    void execute_shouldAnalyzeThoughtsAndDelegateToCommonRecommendationUseCase() {
        EmotionalAnalysisResult analysis = analysis(
                true,
                EmotionType.ANXIETY,
                -0.75,
                0.85,
                -0.70,
                0.90,
                "calmarme y bajar la ansiedad"
        );
        emotionalAnalysisPort.result = analysis;
        recommendationsUseCase.result = result(item(ActivityType.RESPIRACION, "Respiracion guiada"));

        CloudRecommendation result = useCase.execute(List.of("me siento muy ansioso", "no puedo parar"));

        assertThat(result.activityType()).isEqualTo("breathing");
        assertThat(result.actionId()).isEqualTo("breathing");
        assertThat(result.redirectUrl()).isEqualTo("/guided-breathing");
        assertThat(emotionalAnalysisPort.userMessage).isEqualTo("me siento muy ansioso\nno puedo parar");

        EmotionalRecommendationQuery query = recommendationsUseCase.query;
        assertThat(query.vad().valence()).isEqualTo(-0.75);
        assertThat(query.vad().arousal()).isEqualTo(0.85);
        assertThat(query.vad().dominance()).isEqualTo(-0.70);
        assertThat(query.intensity()).isEqualTo(0.90);
        assertThat(query.userGoal()).isEqualTo("calmarme y bajar la ansiedad");
    }

    @Test
    void execute_shouldPassUserIdToCommonRecommendationUseCase_whenUserIdIsAvailable() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.7, 0.2, -0.5, 0.8, "soltar pensamientos");
        recommendationsUseCase.result = result(item(ActivityType.NUBE, "Nubes emocionales"));

        useCase.execute(List.of("quiero soltar esto"), 9L);

        assertThat(recommendationsUseCase.query.userId()).isEqualTo(9L);
    }

    @Test
    void execute_shouldForceRecommendationWithChatPolicy_whenAnalysisDoesNotRecommend() {
        EmotionalAnalysisResult neutral = analysis(
                false,
                EmotionType.NEUTRAL,
                0.0,
                0.0,
                0.0,
                0.1,
                null
        );
        emotionalAnalysisPort.result = neutral;
        recommendationsUseCase.result = result(item(ActivityType.DIARIO, "Diario emocional"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento suelto"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(recommendationsUseCase.query.intensity()).isEqualTo(0.35);
        assertThat(recommendationsUseCase.query.userGoal()).isEqualTo("recibir una actividad de bienestar");
    }

    @Test
    void execute_shouldMapCommonCloudRecommendationToExistingCloudContract() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.7, 0.2, -0.5, 0.8, "soltar pensamientos");
        recommendationsUseCase.result = result(item(ActivityType.NUBE, "Nubes emocionales"));

        CloudRecommendation result = useCase.execute(List.of("quiero soltar esto"));

        assertThat(result.activityType()).isEqualTo("clouds");
        assertThat(result.actionId()).isEqualTo("clouds");
        assertThat(result.title()).isEqualTo("Nubes emocionales");
        assertThat(result.redirectUrl()).isEqualTo("/clouds");
    }

    @Test
    void execute_shouldMapCommonBubbleRecommendationToExistingCloudContract() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.STRESS, -0.4, 0.5, -0.3, 0.7, "distraerme");
        recommendationsUseCase.result = result(item(ActivityType.BURBUJA, "Burbujas"));

        CloudRecommendation result = useCase.execute(List.of("tengo tension acumulada"));

        assertThat(result.activityType()).isEqualTo("bubbles");
        assertThat(result.actionId()).isEqualTo("bubbles");
        assertThat(result.redirectUrl()).isEqualTo("/bubbles");
    }

    @Test
    void execute_shouldReturnFallback_whenCommonRecommendationUseCaseReturnsNoRecommendations() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.5, 0.3, -0.4, 0.7, "procesar");
        recommendationsUseCase.result = new EmotionalRecommendationResult(List.of(), false);

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenAnalysisFails() {
        emotionalAnalysisPort.exception = new RuntimeException("Proveedor IA no disponible");

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    private EmotionalAnalysisResult analysis(
            boolean shouldRecommend,
            EmotionType emotion,
            double valence,
            double arousal,
            double dominance,
            double intensity,
            String userGoal
    ) {
        return new EmotionalAnalysisResult(
                shouldRecommend,
                emotion,
                0.9,
                valence,
                arousal,
                dominance,
                intensity,
                userGoal,
                "analisis"
        );
    }

    private EmotionalRecommendationResult result(EmotionalRecommendationItem item) {
        return new EmotionalRecommendationResult(List.of(item), false);
    }

    private EmotionalRecommendationItem item(ActivityType type, String title) {
        return new EmotionalRecommendationItem(
                10L,
                type,
                title,
                "Descripcion comun",
                0.9,
                "Razon comun"
        );
    }

    private static class CapturingEmotionalAnalysisPort implements EmotionalAnalysisPort {

        private EmotionalAnalysisResult result = EmotionalAnalysisResult.neutral();
        private RuntimeException exception;
        private String userMessage;

        @Override
        public EmotionalAnalysisResult analyze(
                String systemPrompt,
                String userMessage,
                List<ConversationMessage> history
        ) {
            if (exception != null) {
                throw exception;
            }
            this.userMessage = userMessage;
            return result;
        }
    }

    private static class CapturingRecommendationsUseCase extends GetEmotionalRecommendationsUseCase {

        private EmotionalRecommendationQuery query;
        private EmotionalRecommendationResult result = new EmotionalRecommendationResult(List.of(), false);

        private CapturingRecommendationsUseCase() {
            super(null, null, null);
        }

        @Override
        public EmotionalRecommendationResult execute(EmotionalRecommendationQuery query) {
            this.query = query;
            return result;
        }
    }
}
