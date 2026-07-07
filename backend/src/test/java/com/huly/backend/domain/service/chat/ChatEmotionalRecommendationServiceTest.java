package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.model.chat.ChatRecommendationOutcome;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.port.EmotionalAnalysisPort;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatEmotionalRecommendationServiceTest {

    private static final String BASE_PROMPT = "base";
    private static final String ANALYSIS_PROMPT = "analysis prompt";

    private EmotionalAnalysisPort emotionalAnalysisPort;
    private PromptBuilderService promptBuilderService;
    private GetEmotionalRecommendationsUseCase recommendationsUseCase;
    private CreateEmotionalEventUseCase createEmotionalEventUseCase;
    private ChatEmotionalRecommendationService service;

    @BeforeEach
    void setUp() {
        emotionalAnalysisPort = mock(EmotionalAnalysisPort.class);
        promptBuilderService = mock(PromptBuilderService.class);
        recommendationsUseCase = mock(GetEmotionalRecommendationsUseCase.class);
        createEmotionalEventUseCase = mock(CreateEmotionalEventUseCase.class);
        service = new ChatEmotionalRecommendationService(
                emotionalAnalysisPort,
                promptBuilderService,
                new ChatEmotionalRecommendationPolicy(),
                recommendationsUseCase,
                createEmotionalEventUseCase,
                new ChatMapper()
        );
    }

    @Test
    @DisplayName("No recomienda ni crea evento cuando el análisis indica que no")
    void recommendShouldNotRecommendOrCreateEventWhenAnalysisSaysNo() {
        List<VectorMemory> memories = List.of(memory("me ayuda escribir"));
        List<ConversationMessage> history = List.of(userMessage("hola"));
        givenPromptBuiltFor(memories);
        givenAnalyzed(history, analysisWithoutRecommendation());

        ChatRecommendationOutcome outcome = recommend("hola", 1L, memories, history, null, false);

        thenNoSuggestedAction(outcome);
        thenAnalysisDoesNotRecommend(outcome);
        thenUseCasesNeverInvoked();
    }

    @Test
    @DisplayName("Recomienda la actividad principal y crea el evento emocional del chatbot")
    void recommendShouldRecommendTopActivityAndCreateChatbotEmotionalEvent() {
        givenPromptBuilt();
        givenAnalysis(griefRecommendationAnalysis());
        givenRecommendations(diaryItem(7L));
        givenCreatedEvent(eventResponse(50L, 3L, 7L));

        ChatRecommendationOutcome outcome = recommend(
                "estoy decaido, se murio mi perro", 3L, List.of(), List.of(), null, false);

        thenSuggestedActivityIs(outcome, 7L);
        thenSuggestedEventIs(outcome, 50L);
        thenRecommendationQueryHas(3L, -0.85, 0.35, -0.75);
        thenChatbotEventCommandHas(
                "estoy decaido, se murio mi perro",
                "GRIEF",
                0.92,
                0.88,
                "sentirse acompanado y aliviar tristeza",
                7L,
                "Diario emocional");
    }

    @Test
    @DisplayName("Sobrescribe el análisis negativo cuando la conversación detecta duelo de alta intensidad")
    void recommendShouldOverrideFalseAnalysisWhenConversationDetectsHighIntensityGrief() {
        givenPromptBuilt();
        givenAnalysis(neutralWithoutRecommendationAnalysis());
        givenRecommendations(diaryItem(2L));
        givenCreatedEvent(eventResponse(60L, 1L, 2L));

        ChatRecommendationOutcome outcome = recommend(
                "Estoy decaido, se murio Rocky y no se como procesarlo. Me siento sin fuerzas.",
                1L,
                List.of(),
                List.of(),
                griefConversationalReply(),
                false);

        thenSuggestedActivityIs(outcome, 2L);
        thenAnalysisRecommends(outcome);
        thenAnalysisEmotionIs(outcome, EmotionType.GRIEF);
        thenAnalysisValenceIs(outcome, -0.85);
        thenEventCommandEmotionAndGoalContain("GRIEF", "duelo", 2L);
    }

    @Test
    @DisplayName("No crea evento cuando la lista de recomendaciones está vacía")
    void recommendShouldNotCreateEventWhenRecommendationListIsEmpty() {
        givenPromptBuilt();
        givenAnalysis(stressRecommendationAnalysis());
        givenRecommendations();

        ChatRecommendationOutcome outcome = recommend(
                "estoy muy estresado", 1L, List.of(), List.of(), null, false);

        thenNoSuggestedAction(outcome);
        thenCreateEventNeverInvoked();
    }

    @Test
    @DisplayName("Recomienda cuando el usuario pide explícitamente una actividad aunque el análisis diga que no")
    void recommendShouldRecommendWhenUserExplicitlyRequestsActivityEvenIfAnalysisSaysNo() {
        givenPromptBuilt();
        givenAnalysis(explicitNeutralAnalysis());
        givenRecommendations(breathingItem(4L));
        givenCreatedEvent(eventResponse(70L, 1L, 4L));

        ChatRecommendationOutcome outcome = recommend(
                "dame una recomendacion de actividad", 1L, List.of(), List.of(), null, true);

        thenSuggestedActivityIs(outcome, 4L);
        thenAnalysisRecommends(outcome);
        thenAnalysisUserGoalIs(outcome, "recibir una actividad de bienestar");
    }

    @Test
    @DisplayName("Usa un análisis neutral y no recomienda cuando el análisis emocional falla")
    void recommendShouldFallBackToNeutralAnalysisWhenAnalysisPortFails() {
        givenPromptBuilt();
        givenAnalysisPortFails();

        ChatRecommendationOutcome outcome = recommend(
                "hola", 1L, List.of(), List.of(), null, false);

        thenNoSuggestedAction(outcome);
        thenAnalysisDoesNotRecommend(outcome);
        thenAnalysisEmotionIs(outcome, EmotionType.NEUTRAL);
        thenUseCasesNeverInvoked();
    }

    @Test
    @DisplayName("Usa un análisis neutral cuando el puerto de análisis devuelve null")
    void recommendShouldFallBackToNeutralAnalysisWhenAnalysisPortReturnsNull() {
        givenPromptBuilt();
        givenAnalysisReturnsNull();

        ChatRecommendationOutcome outcome = recommend(
                "hola", 1L, List.of(), List.of(), null, false);

        thenNoSuggestedAction(outcome);
        thenAnalysisDoesNotRecommend(outcome);
        thenAnalysisEmotionIs(outcome, EmotionType.NEUTRAL);
        thenUseCasesNeverInvoked();
    }

    @Test
    @DisplayName("Devuelve el análisis sin acción cuando la generación de recomendaciones falla")
    void recommendShouldReturnNoneWhenRecommendationGenerationFails() {
        givenPromptBuilt();
        givenAnalysis(griefRecommendationAnalysis());
        givenRecommendationsUseCaseFails();

        ChatRecommendationOutcome outcome = recommend(
                "estoy decaido, se murio mi perro", 3L, List.of(), List.of(), null, false);

        thenNoSuggestedAction(outcome);
        thenAnalysisRecommends(outcome);
        thenCreateEventNeverInvoked();
    }

    // Nota: la rama `analysis == null` de logAnalysisResult es inalcanzable desde recommend(),
    // porque analyze() nunca devuelve null (siempre retorna EmotionalAnalysisResult.neutral()).

    // --- arrange ---
    private void givenPromptBuiltFor(List<VectorMemory> memories) {
        when(promptBuilderService.buildEmotionalAnalysisPrompt(BASE_PROMPT, memories)).thenReturn(ANALYSIS_PROMPT);
    }

    private void givenPromptBuilt() {
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn(ANALYSIS_PROMPT);
    }

    private void givenAnalyzed(List<ConversationMessage> history, EmotionalAnalysisResult result) {
        when(emotionalAnalysisPort.analyze(ANALYSIS_PROMPT, "hola", history)).thenReturn(result);
    }

    private void givenAnalysis(EmotionalAnalysisResult result) {
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(result);
    }

    private void givenAnalysisReturnsNull() {
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(null);
    }

    private void givenAnalysisPortFails() {
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenThrow(new RuntimeException("boom"));
    }

    private void givenRecommendations(EmotionalRecommendationItem... items) {
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenReturn(new GetEmotionalRecommendationsResponse(List.of(items), false));
    }

    private void givenRecommendationsUseCaseFails() {
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenThrow(new RuntimeException("boom"));
    }

    private void givenCreatedEvent(EmotionalEventResponse event) {
        when(createEmotionalEventUseCase.execute(any(CreateEmotionalEventRequest.class))).thenReturn(event);
    }

    private EmotionalAnalysisResult analysisWithoutRecommendation() {
        return new EmotionalAnalysisResult(false, EmotionType.NEUTRAL, 0.8, 0.0, 0.0, 0.0, 0.1, null, null);
    }

    private EmotionalAnalysisResult griefRecommendationAnalysis() {
        return new EmotionalAnalysisResult(
                true, EmotionType.GRIEF, 0.92, -0.85, 0.35, -0.75, 0.88,
                "sentirse acompanado y aliviar tristeza", "perdida significativa");
    }

    private EmotionalAnalysisResult neutralWithoutRecommendationAnalysis() {
        return new EmotionalAnalysisResult(
                false, EmotionType.NEUTRAL, 0.7, 0.0, 0.0, 0.0, 0.2, null, "El modelo estructurado no recomendo");
    }

    private EmotionalAnalysisResult stressRecommendationAnalysis() {
        return new EmotionalAnalysisResult(
                true, EmotionType.STRESS, 0.9, -0.5, 0.7, -0.4, 0.8, "calmarme", "estres claro");
    }

    private EmotionalAnalysisResult explicitNeutralAnalysis() {
        return new EmotionalAnalysisResult(
                false, EmotionType.NEUTRAL, 0.4, 0.0, 0.0, 0.0, 0.1, null, "pedido neutro");
    }

    private EmotionalRecommendationItem diaryItem(Long activityId) {
        return new EmotionalRecommendationItem(
                activityId,
                ActivityType.DIARY,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                0.95,
                "Recomendada para procesar la emocion",
                "/diary");
    }

    private EmotionalRecommendationItem breathingItem(Long activityId) {
        return new EmotionalRecommendationItem(
                activityId,
                ActivityType.BREATHING,
                "Respiracion guiada",
                "Una practica breve para regularte",
                0.88,
                "Puede ayudar a empezar",
                "/guided-breathing");
    }

    private ChatReply griefConversationalReply() {
        return new ChatReply("Siento mucho lo de Rocky", EmotionType.GRIEF, 8, false, null);
    }

    private EmotionalEventResponse eventResponse(Long id, Long userId, Long recommendedActivityId) {
        Instant now = Instant.now();
        return new EmotionalEventResponse(
                id,
                userId,
                EmotionalEventSource.CHATBOT,
                null,
                "GRIEF",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                recommendedActivityId,
                null,
                (RecommendationDecision) null,
                null,
                null,
                now,
                now
        );
    }

    private VectorMemory memory(String content) {
        return new VectorMemory("id", 1L, null, null, content, null, 0.8);
    }

    private ConversationMessage userMessage(String content) {
        return ConversationMessage.of(MessageRole.USER, content);
    }

    // --- act ---
    private ChatRecommendationOutcome recommend(
            String message,
            Long userId,
            List<VectorMemory> memories,
            List<ConversationMessage> history,
            ChatReply conversationalReply,
            boolean explicitActivityRequest) {
        return service.recommend(
                message, userId, BASE_PROMPT, memories, history, conversationalReply, explicitActivityRequest);
    }

    // --- assert ---
    private void thenNoSuggestedAction(ChatRecommendationOutcome outcome) {
        assertThat(outcome.suggestedAction()).isNull();
    }

    private void thenSuggestedActivityIs(ChatRecommendationOutcome outcome, Long activityId) {
        assertThat(outcome.suggestedAction()).isNotNull();
        assertThat(outcome.suggestedAction().activityId()).isEqualTo(activityId);
    }

    private void thenSuggestedEventIs(ChatRecommendationOutcome outcome, Long eventId) {
        assertThat(outcome.suggestedAction().emotionalEventId()).isEqualTo(eventId);
    }

    private void thenAnalysisDoesNotRecommend(ChatRecommendationOutcome outcome) {
        assertThat(outcome.analysis().shouldRecommend()).isFalse();
    }

    private void thenAnalysisRecommends(ChatRecommendationOutcome outcome) {
        assertThat(outcome.analysis().shouldRecommend()).isTrue();
    }

    private void thenAnalysisEmotionIs(ChatRecommendationOutcome outcome, EmotionType emotion) {
        assertThat(outcome.analysis().detectedEmotion()).isEqualTo(emotion);
    }

    private void thenAnalysisValenceIs(ChatRecommendationOutcome outcome, double valence) {
        assertThat(outcome.analysis().valence()).isEqualTo(valence);
    }

    private void thenAnalysisUserGoalIs(ChatRecommendationOutcome outcome, String userGoal) {
        assertThat(outcome.analysis().userGoal()).isEqualTo(userGoal);
    }

    private void thenRecommendationQueryHas(Long userId, double valence, double arousal, double dominance) {
        ArgumentCaptor<GetEmotionalRecommendationsRequest> captor =
                ArgumentCaptor.forClass(GetEmotionalRecommendationsRequest.class);
        verify(recommendationsUseCase).execute(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().valence()).isEqualTo(valence);
        assertThat(captor.getValue().arousal()).isEqualTo(arousal);
        assertThat(captor.getValue().dominance()).isEqualTo(dominance);
    }

    private void thenChatbotEventCommandHas(
            String inputText,
            String detectedEmotion,
            double confidence,
            double intensity,
            String userGoal,
            Long recommendedActivityId,
            String recommendationFragment) {
        ArgumentCaptor<CreateEmotionalEventRequest> captor =
                ArgumentCaptor.forClass(CreateEmotionalEventRequest.class);
        verify(createEmotionalEventUseCase).execute(captor.capture());
        CreateEmotionalEventRequest command = captor.getValue();
        assertThat(command.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(command.inputText()).isEqualTo(inputText);
        assertThat(command.detectedEmotion()).isEqualTo(detectedEmotion);
        assertThat(command.confidence()).isEqualTo(confidence);
        assertThat(command.intensity()).isEqualTo(intensity);
        assertThat(command.userGoal()).isEqualTo(userGoal);
        assertThat(command.recommendedActivityId()).isEqualTo(recommendedActivityId);
        assertThat(command.chosenActivityId()).isNull();
        assertThat(command.generatedRecommendation()).contains(recommendationFragment);
    }

    private void thenEventCommandEmotionAndGoalContain(
            String detectedEmotion, String userGoalFragment, Long recommendedActivityId) {
        ArgumentCaptor<CreateEmotionalEventRequest> captor =
                ArgumentCaptor.forClass(CreateEmotionalEventRequest.class);
        verify(createEmotionalEventUseCase).execute(captor.capture());
        assertThat(captor.getValue().detectedEmotion()).isEqualTo(detectedEmotion);
        assertThat(captor.getValue().userGoal()).contains(userGoalFragment);
        assertThat(captor.getValue().recommendedActivityId()).isEqualTo(recommendedActivityId);
    }

    private void thenUseCasesNeverInvoked() {
        verify(recommendationsUseCase, never()).execute(any());
        verify(createEmotionalEventUseCase, never()).execute(any());
    }

    private void thenCreateEventNeverInvoked() {
        verify(createEmotionalEventUseCase, never()).execute(any());
    }
}
