package com.huly.backend.domain.useCase.lanternRecommendation;

import com.huly.backend.domain.model.LanternRecommendation;
import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.EmotionalRecommendationService;
import com.huly.backend.domain.service.chat.ChatEmotionalRecommendationPolicy;
import com.huly.backend.domain.service.chat.PromptBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetLanternRecommendationUseCaseTest {

    private CapturingEmotionalAnalysisPort emotionalAnalysisPort;
    private CapturingRecommendationService recommendationService;
    private StaticActivityRepository activityRepository;
    private StaticEmotionalEventRepository emotionalEventRepository;
    private GetLanternRecommendationUseCase useCase;

    @BeforeEach
    void setUp() {
        emotionalAnalysisPort = new CapturingEmotionalAnalysisPort();
        recommendationService = new CapturingRecommendationService();
        activityRepository = new StaticActivityRepository();
        emotionalEventRepository = new StaticEmotionalEventRepository();
        useCase = new GetLanternRecommendationUseCase(
                emotionalAnalysisPort,
                new PromptBuilderService(),
                new ChatEmotionalRecommendationPolicy(),
                recommendationService,
                activityRepository,
                emotionalEventRepository
        );
    }

    @Test
    void execute_shouldAnalyzeThoughtsAndDelegateToSharedRecommendationService() {
        EmotionalAnalysisResult analysis = analysis(
                true, EmotionType.ANXIETY, -0.75, 0.85, -0.70, 0.90, "calmarme y bajar la ansiedad"
        );
        emotionalAnalysisPort.result = analysis;
        recommendationService.result = result(item(ActivityType.RESPIRACION, "Respiracion guiada"));

        LanternRecommendation result = useCase.execute(List.of("me siento muy ansioso", "no puedo parar"));

        assertThat(result.activityType()).isEqualTo("breathing");
        assertThat(result.actionId()).isEqualTo("breathing");
        assertThat(result.redirectUrl()).isEqualTo("/guided-breathing");
        assertThat(emotionalAnalysisPort.userMessage).isEqualTo("me siento muy ansioso\nno puedo parar");

        EmotionalRecommendationQuery query = recommendationService.query;
        assertThat(query.vad().valence()).isEqualTo(-0.75);
        assertThat(query.vad().arousal()).isEqualTo(0.85);
        assertThat(query.vad().dominance()).isEqualTo(-0.70);
        assertThat(query.intensity()).isEqualTo(0.90);
        assertThat(query.userGoal()).isEqualTo("calmarme y bajar la ansiedad");
        assertThat(recommendationService.activities).isEqualTo(activityRepository.activities);
        assertThat(recommendationService.userHistory).isEmpty();
    }

    @Test
    void execute_shouldPassUserIdAndHistoryToSharedRecommendationService_whenUserIdIsAvailable() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.7, 0.2, -0.5, 0.8, "soltar pensamientos");
        recommendationService.result = result(item(ActivityType.NUBE, "Faroles emocionales"));
        emotionalEventRepository.history = List.of(EmotionalEvent.builder().userId(9L).build());

        useCase.execute(List.of("quiero soltar esto"), 9L);

        assertThat(recommendationService.query.userId()).isEqualTo(9L);
        assertThat(recommendationService.userHistory).hasSize(1);
    }

    @Test
    void execute_shouldForceRecommendationWithChatPolicy_whenAnalysisDoesNotRecommend() {
        EmotionalAnalysisResult neutral = analysis(false, EmotionType.NEUTRAL, 0.0, 0.0, 0.0, 0.1, null);
        emotionalAnalysisPort.result = neutral;
        recommendationService.result = result(item(ActivityType.DIARIO, "Diario emocional"));

        LanternRecommendation result = useCase.execute(List.of("pensamiento suelto"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(recommendationService.query.intensity()).isEqualTo(0.35);
        assertThat(recommendationService.query.userGoal()).isEqualTo("recibir una actividad de bienestar");
    }

    @Test
    void execute_shouldMapNubeRecommendationToLanternContract() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.7, 0.2, -0.5, 0.8, "soltar pensamientos");
        recommendationService.result = result(item(ActivityType.NUBE, "Faroles emocionales"));

        LanternRecommendation result = useCase.execute(List.of("quiero soltar esto"));

        assertThat(result.activityType()).isEqualTo("lanterns");
        assertThat(result.actionId()).isEqualTo("lanterns");
        assertThat(result.title()).isEqualTo("Faroles emocionales");
        assertThat(result.redirectUrl()).isEqualTo("/lanterns");
    }

    @Test
    void execute_shouldMapBubbleRecommendationToLanternContract() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.STRESS, -0.4, 0.5, -0.3, 0.7, "distraerme");
        recommendationService.result = result(item(ActivityType.BURBUJA, "Burbujas"));

        LanternRecommendation result = useCase.execute(List.of("tengo tension acumulada"));

        assertThat(result.activityType()).isEqualTo("bubbles");
        assertThat(result.actionId()).isEqualTo("bubbles");
        assertThat(result.redirectUrl()).isEqualTo("/bubbles");
    }

    @Test
    void execute_shouldReturnFallback_whenSharedRecommendationServiceReturnsNoRecommendations() {
        emotionalAnalysisPort.result =
                analysis(true, EmotionType.SADNESS, -0.5, 0.3, -0.4, 0.7, "procesar");
        recommendationService.result = new EmotionalRecommendationResult(List.of(), false);

        LanternRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenAnalysisFails() {
        emotionalAnalysisPort.exception = new RuntimeException("Proveedor IA no disponible");

        LanternRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    private EmotionalAnalysisResult analysis(
            boolean shouldRecommend, EmotionType emotion,
            double valence, double arousal, double dominance, double intensity, String userGoal
    ) {
        return new EmotionalAnalysisResult(
                shouldRecommend, emotion, 0.9, valence, arousal, dominance, intensity, userGoal, "analisis"
        );
    }

    private EmotionalRecommendationResult result(EmotionalRecommendationItem item) {
        return new EmotionalRecommendationResult(List.of(item), false);
    }

    private EmotionalRecommendationItem item(ActivityType type, String title) {
        return new EmotionalRecommendationItem(10L, type, title, "Descripcion comun", 0.9, "Razon comun");
    }

    private static class CapturingEmotionalAnalysisPort implements EmotionalAnalysisPort {
        private EmotionalAnalysisResult result = EmotionalAnalysisResult.neutral();
        private RuntimeException exception;
        private String userMessage;

        @Override
        public EmotionalAnalysisResult analyze(String systemPrompt, String userMessage, List<ConversationMessage> history) {
            if (exception != null) throw exception;
            this.userMessage = userMessage;
            return result;
        }
    }

    private static class CapturingRecommendationService extends EmotionalRecommendationService {

        private EmotionalRecommendationQuery query;
        private List<Activity> activities = List.of();
        private List<EmotionalEvent> userHistory = List.of();
        private EmotionalRecommendationResult result = new EmotionalRecommendationResult(List.of(), false);

        @Override
        public EmotionalRecommendationResult recommend(
                EmotionalRecommendationQuery query,
                List<Activity> activities,
                List<EmotionalEvent> userHistory
        ) {
            this.query = query;
            this.activities = activities;
            this.userHistory = userHistory;
            return result;
        }
    }

    private static class StaticActivityRepository implements ActivityRepository {
        private final List<Activity> activities = List.of(Activity.builder().id(1L).type(ActivityType.RESPIRACION).build());

        @Override
        public List<Activity> findAll() {
            return activities;
        }

        @Override
        public boolean existsById(Long id) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StaticEmotionalEventRepository implements EmotionalEventRepository {
        private List<EmotionalEvent> history = List.of();

        @Override
        public List<EmotionalEvent> findRecentRecommendationHistoryByUserId(Long userId, int limit) {
            return history;
        }

        @Override public EmotionalEvent save(EmotionalEvent emotionalEvent) { throw new UnsupportedOperationException(); }
        @Override public List<EmotionalEvent> findByUserId(Long userId) { throw new UnsupportedOperationException(); }
        @Override public List<EmotionalEvent> findRecommendationEventsByUserId(Long userId) { throw new UnsupportedOperationException(); }
        @Override public java.util.Optional<EmotionalEvent> findById(Long id) { throw new UnsupportedOperationException(); }
    }
}
