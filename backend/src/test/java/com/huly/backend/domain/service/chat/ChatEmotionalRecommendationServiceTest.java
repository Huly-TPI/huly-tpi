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
        List<ConversationMessage> history = List.of(ConversationMessage.of(MessageRole.USER, "hola"));
        when(promptBuilderService.buildEmotionalAnalysisPrompt("base", memories)).thenReturn("analysis prompt");
        when(emotionalAnalysisPort.analyze("analysis prompt", "hola", history))
                .thenReturn(new EmotionalAnalysisResult(false, EmotionType.NEUTRAL, 0.8, 0.0, 0.0, 0.0, 0.1, null, null));

        ChatRecommendationOutcome outcome = service.recommend("hola", 1L, "base", memories, history, null, false);

        assertThat(outcome.suggestedAction()).isNull();
        assertThat(outcome.analysis().shouldRecommend()).isFalse();
        verify(recommendationsUseCase, never()).execute(any());
        verify(createEmotionalEventUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("Recomienda la actividad principal y crea el evento emocional del chatbot")
    void recommendShouldRecommendTopActivityAndCreateChatbotEmotionalEvent() {
        EmotionalAnalysisResult analysis = new EmotionalAnalysisResult(
                true,
                EmotionType.GRIEF,
                0.92,
                -0.85,
                0.35,
                -0.75,
                0.88,
                "sentirse acompanado y aliviar tristeza",
                "perdida significativa"
        );
        EmotionalRecommendationItem item = new EmotionalRecommendationItem(
                7L,
                ActivityType.DIARY,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                0.95,
                "Recomendada para procesar la emocion",
                "/diary"
        );
        EmotionalEventResponse saved = eventResponse(50L, 3L, 7L);
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn("analysis prompt");
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(analysis);
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenReturn(new GetEmotionalRecommendationsResponse(List.of(item), false));
        when(createEmotionalEventUseCase.execute(any(CreateEmotionalEventRequest.class))).thenReturn(saved);

        ChatRecommendationOutcome outcome = service.recommend(
                "estoy decaido, se murio mi perro",
                3L,
                "base",
                List.of(),
                List.of(),
                null,
                false
        );

        assertThat(outcome.suggestedAction()).isNotNull();
        assertThat(outcome.suggestedAction().activityId()).isEqualTo(7L);
        assertThat(outcome.suggestedAction().emotionalEventId()).isEqualTo(50L);

        ArgumentCaptor<GetEmotionalRecommendationsRequest> queryCaptor =
                ArgumentCaptor.forClass(GetEmotionalRecommendationsRequest.class);
        verify(recommendationsUseCase).execute(queryCaptor.capture());
        assertThat(queryCaptor.getValue().userId()).isEqualTo(3L);
        assertThat(queryCaptor.getValue().valence()).isEqualTo(-0.85);
        assertThat(queryCaptor.getValue().arousal()).isEqualTo(0.35);
        assertThat(queryCaptor.getValue().dominance()).isEqualTo(-0.75);

        ArgumentCaptor<CreateEmotionalEventRequest> commandCaptor =
                ArgumentCaptor.forClass(CreateEmotionalEventRequest.class);
        verify(createEmotionalEventUseCase).execute(commandCaptor.capture());
        CreateEmotionalEventRequest command = commandCaptor.getValue();
        assertThat(command.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(command.inputText()).isEqualTo("estoy decaido, se murio mi perro");
        assertThat(command.detectedEmotion()).isEqualTo("GRIEF");
        assertThat(command.confidence()).isEqualTo(0.92);
        assertThat(command.intensity()).isEqualTo(0.88);
        assertThat(command.userGoal()).isEqualTo("sentirse acompanado y aliviar tristeza");
        assertThat(command.recommendedActivityId()).isEqualTo(7L);
        assertThat(command.chosenActivityId()).isNull();
        assertThat(command.generatedRecommendation()).contains("Diario emocional");
    }

    @Test
    @DisplayName("Sobrescribe el análisis negativo cuando la conversación detecta duelo de alta intensidad")
    void recommendShouldOverrideFalseAnalysisWhenConversationDetectsHighIntensityGrief() {
        EmotionalAnalysisResult analysis = new EmotionalAnalysisResult(
                false,
                EmotionType.NEUTRAL,
                0.7,
                0.0,
                0.0,
                0.0,
                0.2,
                null,
                "El modelo estructurado no recomendo"
        );
        EmotionalRecommendationItem item = new EmotionalRecommendationItem(
                2L,
                ActivityType.DIARY,
                "Diario emocional",
                "Un espacio para ordenar pensamientos",
                0.91,
                "Recomendada para procesar la emocion",
                "/diary"
        );
        EmotionalEventResponse saved = eventResponse(60L, 1L, 2L);
        ChatReply conversationalReply = new ChatReply(
                "Siento mucho lo de Rocky",
                EmotionType.GRIEF,
                8,
                false,
                null
        );
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn("analysis prompt");
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(analysis);
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenReturn(new GetEmotionalRecommendationsResponse(List.of(item), false));
        when(createEmotionalEventUseCase.execute(any(CreateEmotionalEventRequest.class))).thenReturn(saved);

        ChatRecommendationOutcome outcome = service.recommend(
                "Estoy decaido, se murio Rocky y no se como procesarlo. Me siento sin fuerzas.",
                1L,
                "base",
                List.of(),
                List.of(),
                conversationalReply,
                false
        );

        assertThat(outcome.suggestedAction()).isNotNull();
        assertThat(outcome.suggestedAction().activityId()).isEqualTo(2L);
        assertThat(outcome.analysis().shouldRecommend()).isTrue();
        assertThat(outcome.analysis().detectedEmotion()).isEqualTo(EmotionType.GRIEF);
        assertThat(outcome.analysis().valence()).isEqualTo(-0.85);

        ArgumentCaptor<CreateEmotionalEventRequest> commandCaptor =
                ArgumentCaptor.forClass(CreateEmotionalEventRequest.class);
        verify(createEmotionalEventUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().detectedEmotion()).isEqualTo("GRIEF");
        assertThat(commandCaptor.getValue().userGoal()).contains("duelo");
        assertThat(commandCaptor.getValue().recommendedActivityId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("No crea evento cuando la lista de recomendaciones está vacía")
    void recommendShouldNotCreateEventWhenRecommendationListIsEmpty() {
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn("analysis prompt");
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(new EmotionalAnalysisResult(
                true,
                EmotionType.STRESS,
                0.9,
                -0.5,
                0.7,
                -0.4,
                0.8,
                "calmarme",
                "estres claro"
        ));
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenReturn(new GetEmotionalRecommendationsResponse(List.of(), false));

        ChatRecommendationOutcome outcome = service.recommend(
                "estoy muy estresado",
                1L,
                "base",
                List.of(),
                List.of(),
                null,
                false);

        assertThat(outcome.suggestedAction()).isNull();
        verify(createEmotionalEventUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("Recomienda cuando el usuario pide explícitamente una actividad aunque el análisis diga que no")
    void recommendShouldRecommendWhenUserExplicitlyRequestsActivityEvenIfAnalysisSaysNo() {
        EmotionalAnalysisResult analysis = new EmotionalAnalysisResult(
                false,
                EmotionType.NEUTRAL,
                0.4,
                0.0,
                0.0,
                0.0,
                0.1,
                null,
                "pedido neutro"
        );
        EmotionalRecommendationItem item = new EmotionalRecommendationItem(
                4L,
                ActivityType.BREATHING,
                "Respiracion guiada",
                "Una practica breve para regularte",
                0.88,
                "Puede ayudar a empezar",
                "/guided-breathing"
        );
        EmotionalEventResponse saved = eventResponse(70L, 1L, 4L);
        when(promptBuilderService.buildEmotionalAnalysisPrompt(any(), any())).thenReturn("analysis prompt");
        when(emotionalAnalysisPort.analyze(any(), any(), any())).thenReturn(analysis);
        when(recommendationsUseCase.execute(any(GetEmotionalRecommendationsRequest.class)))
                .thenReturn(new GetEmotionalRecommendationsResponse(List.of(item), false));
        when(createEmotionalEventUseCase.execute(any(CreateEmotionalEventRequest.class))).thenReturn(saved);

        ChatRecommendationOutcome outcome = service.recommend(
                "dame una recomendacion de actividad",
                1L,
                "base",
                List.of(),
                List.of(),
                null,
                true
        );

        assertThat(outcome.suggestedAction()).isNotNull();
        assertThat(outcome.suggestedAction().activityId()).isEqualTo(4L);
        assertThat(outcome.analysis().shouldRecommend()).isTrue();
        assertThat(outcome.analysis().userGoal()).isEqualTo("recibir una actividad de bienestar");
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
}
