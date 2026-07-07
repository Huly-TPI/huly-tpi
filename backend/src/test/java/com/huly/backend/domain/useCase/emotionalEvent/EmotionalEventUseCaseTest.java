package com.huly.backend.domain.useCase.emotionalEvent;

import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventDecisionRequest;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.emotionalEvent.CreateEmotionalEventMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventDecisionMapper;
import com.huly.backend.domain.mapper.emotionalEvent.UpdateEmotionalEventFeedbackMapper;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test agrupado que cubre los tres casos de uso de eventos emocionales:
 * creación, actualización de decisión y actualización de feedback.
 */
@ExtendWith(MockitoExtension.class)
class EmotionalEventUseCaseTest {

    private static final Long EVENT_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final Long RECOMMENDED_ACTIVITY_ID = 1L;
    private static final Long OTHER_ACTIVITY_ID = 4L;
    private static final Long MISSING_ACTIVITY_ID = 99L;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    private CreateEmotionalEventUseCase createUseCase;
    private UpdateEmotionalEventDecisionUseCase decisionUseCase;
    private UpdateEmotionalEventFeedbackUseCase feedbackUseCase;

    private CreateEmotionalEventRequest createRequest;
    private UpdateEmotionalEventDecisionRequest decisionRequest;
    private UpdateEmotionalEventFeedbackRequest feedbackRequest;

    @BeforeEach
    void setUp() {
        createUseCase = new CreateEmotionalEventUseCase(
                emotionalEventRepository, activityRepository, new CreateEmotionalEventMapper());
        decisionUseCase = new UpdateEmotionalEventDecisionUseCase(
                emotionalEventRepository,
                activityRepository,
                userVectorMemoryService,
                chatMessageRepository,
                new UpdateEmotionalEventDecisionMapper());
        feedbackUseCase = new UpdateEmotionalEventFeedbackUseCase(
                emotionalEventRepository, new UpdateEmotionalEventFeedbackMapper());
    }

    // ================= CreateEmotionalEventUseCase =================

    @Test
    @DisplayName("Crea y guarda un evento emocional válido")
    void createShouldSaveValidEmotionalEvent() {
        // --- arrange ---
        givenValidCreateRequest();
        givenActivityExists(RECOMMENDED_ACTIVITY_ID);
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = create();

        // --- assert ---
        thenCreatedEventMatchesRequest(result);
    }

    @Test
    @DisplayName("Crea un evento aunque los valores VAD y las actividades sean nulos")
    void createShouldSaveWhenOptionalValuesAreNull() {
        // --- arrange ---
        givenCreateRequestWithNullOptionalValues();
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = create();

        // --- assert ---
        thenCreatedEventHasNullOptionalValues(result);
    }

    @Test
    @DisplayName("Rechaza la creación cuando el source es nulo")
    void createShouldThrowWhenSourceIsNull() {
        // --- arrange ---
        givenCreateRequestWithNullSource();

        // --- assert ---
        thenCreateThrowsBusinessRule();
    }

    @Test
    @DisplayName("Rechaza la creación cuando la emoción detectada es nula")
    void createShouldThrowWhenDetectedEmotionIsNull() {
        // --- arrange ---
        givenCreateRequestWithDetectedEmotion(null);

        // --- assert ---
        thenCreateThrowsBusinessRule();
    }

    @Test
    @DisplayName("Rechaza la creación cuando la emoción detectada está en blanco")
    void createShouldThrowWhenDetectedEmotionIsBlank() {
        // --- arrange ---
        givenCreateRequestWithDetectedEmotion("   ");

        // --- assert ---
        thenCreateThrowsBusinessRule();
    }

    @Test
    @DisplayName("Rechaza la creación cuando la confianza está fuera de rango")
    void createShouldThrowWhenConfidenceOutOfRange() {
        // --- arrange ---
        givenCreateRequestWithConfidenceOutOfRange();

        // --- assert ---
        thenCreateThrowsBusinessRuleContaining("confidence");
    }

    @Test
    @DisplayName("Rechaza la creación cuando la valencia está por debajo del mínimo")
    void createShouldThrowWhenValenceBelowRange() {
        // --- arrange ---
        givenCreateRequestWithValenceBelowRange();

        // --- assert ---
        thenCreateThrowsBusinessRuleContaining("valence");
    }

    @Test
    @DisplayName("Rechaza la creación cuando la actividad recomendada no existe")
    void createShouldThrowWhenRecommendedActivityDoesNotExist() {
        // --- arrange ---
        givenCreateRequestWithMissingRecommendedActivity();
        givenActivityDoesNotExist(MISSING_ACTIVITY_ID);

        // --- assert ---
        thenCreateThrowsBusinessRuleContaining("recommendedActivityId");
    }

    @Test
    @DisplayName("Rechaza la creación cuando la actividad elegida no existe")
    void createShouldThrowWhenChosenActivityDoesNotExist() {
        // --- arrange ---
        givenCreateRequestWithMissingChosenActivity();
        givenActivityDoesNotExist(MISSING_ACTIVITY_ID);

        // --- assert ---
        thenCreateThrowsBusinessRuleContaining("chosenActivityId");
    }

    // ================= UpdateEmotionalEventDecisionUseCase =================
    // Los null-guards del bloque de persistencia se ejercitan mockeando el repositorio para que
    // devuelva un evento persistido incompleto (userId/id/decision nulos). La única rama que NO se
    // cubre es saved == null: al final el flujo llama mapper.toResponse(saved) y con saved nulo eso
    // lanzaría NPE, por lo que esa rama defensiva solo sería alcanzable con un test que afirme el NPE.

    @Test
    @DisplayName("Acepta usando la actividad recomendada cuando no se envía una elegida")
    void updateDecisionShouldSaveAcceptedUsingRecommendedActivityWhenChosenIsMissing() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.ACCEPTED, null);
        givenPersistedEvent();
        givenActivityExists(RECOMMENDED_ACTIVITY_ID);
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateDecision();

        // --- assert ---
        thenDecisionSaved(result, RecommendationDecision.ACCEPTED, RECOMMENDED_ACTIVITY_ID);
    }

    @Test
    @DisplayName("Acepta usando la actividad elegida cuando se envía explícitamente")
    void updateDecisionShouldSaveAcceptedUsingProvidedChosenActivity() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.ACCEPTED, OTHER_ACTIVITY_ID);
        givenPersistedEvent();
        givenActivityExists(OTHER_ACTIVITY_ID);
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateDecision();

        // --- assert ---
        thenDecisionSaved(result, RecommendationDecision.ACCEPTED, OTHER_ACTIVITY_ID);
    }

    @Test
    @DisplayName("Guarda IGNORED descartando la actividad elegida")
    void updateDecisionShouldSaveIgnoredWithoutChosenActivity() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.IGNORED, RECOMMENDED_ACTIVITY_ID);
        givenPersistedEvent();
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateDecision();

        // --- assert ---
        thenDecisionSaved(result, RecommendationDecision.IGNORED, null);
    }

    @Test
    @DisplayName("Guarda IGNORED cuando el evento no tiene datos de recomendación")
    void updateDecisionShouldSaveIgnoredWhenEventHasNoRecommendationData() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.IGNORED, null);
        givenPersistedEventWithoutRecommendationData();
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateDecision();

        // --- assert ---
        thenDecisionSaved(result, RecommendationDecision.IGNORED, null);
    }

    @Test
    @DisplayName("Guarda CHOSE_OTHER con la actividad elegida")
    void updateDecisionShouldSaveChoseOtherWithChosenActivity() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.CHOSE_OTHER, OTHER_ACTIVITY_ID);
        givenPersistedEvent();
        givenActivityExists(OTHER_ACTIVITY_ID);
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateDecision();

        // --- assert ---
        thenDecisionSaved(result, RecommendationDecision.CHOSE_OTHER, OTHER_ACTIVITY_ID);
    }

    @Test
    @DisplayName("Exige actividad elegida para CHOSE_OTHER")
    void updateDecisionShouldRequireChosenActivityForChoseOther() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.CHOSE_OTHER, null);
        givenPersistedEvent();

        // --- assert ---
        thenDecisionThrowsBusinessRule();
    }

    @Test
    @DisplayName("Rechaza la decisión cuando es nula")
    void updateDecisionShouldThrowWhenDecisionIsNull() {
        // --- arrange ---
        givenDecisionRequest(null, null);

        // --- assert ---
        thenDecisionThrowsBusinessRule();
    }

    @Test
    @DisplayName("Falla cuando el evento a decidir no existe")
    void updateDecisionShouldThrowWhenEventNotFound() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.ACCEPTED, null);
        givenEventNotFound();

        // --- assert ---
        thenDecisionThrowsResourceNotFound();
    }

    @Test
    @DisplayName("Rechaza CHOSE_OTHER cuando la actividad elegida no existe")
    void updateDecisionShouldThrowWhenChosenActivityDoesNotExist() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.CHOSE_OTHER, MISSING_ACTIVITY_ID);
        givenPersistedEvent();
        givenActivityDoesNotExist(MISSING_ACTIVITY_ID);

        // --- assert ---
        thenDecisionThrowsBusinessRule();
    }

    @Test
    @DisplayName("Omite los efectos de persistencia cuando el evento guardado no tiene userId")
    void updateDecisionShouldSkipSideEffectsWhenSavedEventHasNoUserId() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.IGNORED, null);
        givenPersistedEvent();
        givenSaveReturns(incompleteSavedEvent(EVENT_ID, null, RecommendationDecision.IGNORED));

        // --- act ---
        updateDecision();

        // --- assert ---
        thenNoPersistenceSideEffects();
    }

    @Test
    @DisplayName("Guarda la memoria con ids de respaldo cuando el evento guardado no tiene id")
    void updateDecisionShouldSaveMemoryWithFallbackIdsWhenSavedEventHasNoId() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.IGNORED, null);
        givenPersistedEvent();
        givenSaveReturns(incompleteSavedEvent(null, USER_ID, RecommendationDecision.IGNORED));

        // --- act ---
        updateDecision();

        // --- assert ---
        thenMemorySavedWithFallbackIdsAndChatDecisionSkipped();
    }

    @Test
    @DisplayName("Omite los efectos de persistencia cuando el evento guardado no tiene decisión")
    void updateDecisionShouldSkipSideEffectsWhenSavedEventHasNoDecision() {
        // --- arrange ---
        givenDecisionRequest(RecommendationDecision.IGNORED, null);
        givenPersistedEvent();
        givenSaveReturns(incompleteSavedEvent(EVENT_ID, USER_ID, null));

        // --- act ---
        updateDecision();

        // --- assert ---
        thenNoPersistenceSideEffects();
    }

    // ================= UpdateEmotionalEventFeedbackUseCase =================

    @Test
    @DisplayName("Guarda el feedback del evento emocional")
    void updateFeedbackShouldSaveFeedback() {
        // --- arrange ---
        givenFeedbackRequest(4, "Me siento un poco mas tranquilo");
        givenPersistedEvent();
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateFeedback();

        // --- assert ---
        thenFeedbackSaved(result, 4, "Me siento un poco mas tranquilo");
    }

    @Test
    @DisplayName("Guarda el feedback aunque el puntaje sea nulo")
    void updateFeedbackShouldSaveWhenScoreIsNull() {
        // --- arrange ---
        givenFeedbackRequest(null, "Sin puntaje");
        givenPersistedEvent();
        givenEmotionalEventIsSavedAsIs();

        // --- act ---
        EmotionalEventResponse result = updateFeedback();

        // --- assert ---
        thenFeedbackSaved(result, null, "Sin puntaje");
    }

    @Test
    @DisplayName("Rechaza el feedback cuando el puntaje es menor a 1")
    void updateFeedbackShouldThrowWhenScoreBelowRange() {
        // --- arrange ---
        givenFeedbackRequest(0, "texto");

        // --- assert ---
        thenFeedbackThrowsBusinessRule();
    }

    @Test
    @DisplayName("Rechaza el feedback cuando el puntaje es mayor a 5")
    void updateFeedbackShouldThrowWhenScoreAboveRange() {
        // --- arrange ---
        givenFeedbackRequest(6, "texto");

        // --- assert ---
        thenFeedbackThrowsBusinessRule();
    }

    @Test
    @DisplayName("Falla cuando el evento a dar feedback no existe")
    void updateFeedbackShouldThrowWhenEventNotFound() {
        // --- arrange ---
        givenFeedbackRequest(4, "texto");
        givenEventNotFound();

        // --- assert ---
        thenFeedbackThrowsResourceNotFound();
    }

    // --- arrange ---

    private void givenValidCreateRequest() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID,
                EmotionalEventSource.CHATBOT,
                "Estoy muy ansioso",
                "ANSIEDAD",
                0.91,
                -0.8,
                0.9,
                -0.7,
                0.85,
                "calmarme",
                "Te recomiendo una respiracion guiada",
                RECOMMENDED_ACTIVITY_ID,
                null);
    }

    private void givenCreateRequestWithNullOptionalValues() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                null, null, null, null, null,
                "calmarme", "Respira", null, null);
    }

    private void givenCreateRequestWithNullSource() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, null, "texto", "ANSIEDAD",
                0.9, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null);
    }

    private void givenCreateRequestWithDetectedEmotion(String detectedEmotion) {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", detectedEmotion,
                0.9, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null);
    }

    private void givenCreateRequestWithConfidenceOutOfRange() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                1.2, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null);
    }

    private void givenCreateRequestWithValenceBelowRange() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                0.9, -1.5, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, null);
    }

    private void givenCreateRequestWithMissingRecommendedActivity() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                0.9, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", MISSING_ACTIVITY_ID, null);
    }

    private void givenCreateRequestWithMissingChosenActivity() {
        createRequest = new CreateEmotionalEventRequest(
                USER_ID, EmotionalEventSource.CHATBOT, "texto", "ANSIEDAD",
                0.9, -0.8, 0.9, -0.7, 0.85,
                "calmarme", "Respira", null, MISSING_ACTIVITY_ID);
    }

    private void givenDecisionRequest(RecommendationDecision decision, Long chosenActivityId) {
        decisionRequest = new UpdateEmotionalEventDecisionRequest(EVENT_ID, decision, chosenActivityId);
    }

    private void givenFeedbackRequest(Integer feedbackScore, String feedbackText) {
        feedbackRequest = new UpdateEmotionalEventFeedbackRequest(EVENT_ID, feedbackScore, feedbackText);
    }

    private void givenActivityExists(Long activityId) {
        when(activityRepository.existsById(activityId)).thenReturn(true);
    }

    private void givenActivityDoesNotExist(Long activityId) {
        when(activityRepository.existsById(activityId)).thenReturn(false);
    }

    private void givenEmotionalEventIsSavedAsIs() {
        when(emotionalEventRepository.save(any(EmotionalEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenSaveReturns(EmotionalEvent saved) {
        when(emotionalEventRepository.save(any(EmotionalEvent.class))).thenReturn(saved);
    }

    private EmotionalEvent incompleteSavedEvent(Long id, Long userId, RecommendationDecision decision) {
        return EmotionalEvent.builder()
                .id(id)
                .userId(userId)
                .source(EmotionalEventSource.CHATBOT)
                .recommendationDecision(decision)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void givenPersistedEvent() {
        when(emotionalEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(persistedEvent()));
    }

    private void givenPersistedEventWithoutRecommendationData() {
        EmotionalEvent event = persistedEvent().toBuilder()
                .recommendedActivityId(null)
                .generatedRecommendation(null)
                .build();
        when(emotionalEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    }

    private void givenEventNotFound() {
        when(emotionalEventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());
    }

    private EmotionalEvent persistedEvent() {
        Instant now = Instant.now();
        return EmotionalEvent.builder()
                .id(EVENT_ID)
                .userId(USER_ID)
                .source(EmotionalEventSource.CHATBOT)
                .inputText("Estoy muy ansioso")
                .detectedEmotion("ANSIEDAD")
                .confidence(0.91)
                .valence(-0.8)
                .arousal(0.9)
                .dominance(-0.7)
                .intensity(0.85)
                .userGoal("calmarme")
                .generatedRecommendation("Respira")
                .recommendedActivityId(RECOMMENDED_ACTIVITY_ID)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // --- act ---

    private EmotionalEventResponse create() {
        return createUseCase.execute(createRequest);
    }

    private EmotionalEventResponse updateDecision() {
        return decisionUseCase.execute(decisionRequest);
    }

    private EmotionalEventResponse updateFeedback() {
        return feedbackUseCase.execute(feedbackRequest);
    }

    // --- assert ---

    private void thenCreatedEventMatchesRequest(EmotionalEventResponse result) {
        assertThat(result.source()).isEqualTo(EmotionalEventSource.CHATBOT);
        assertThat(result.detectedEmotion()).isEqualTo("ANSIEDAD");
        assertThat(result.recommendedActivityId()).isEqualTo(RECOMMENDED_ACTIVITY_ID);
        assertThat(result.createdAt()).isNotNull();
        verify(emotionalEventRepository).save(any(EmotionalEvent.class));
    }

    private void thenCreatedEventHasNullOptionalValues(EmotionalEventResponse result) {
        assertThat(result.confidence()).isNull();
        assertThat(result.valence()).isNull();
        assertThat(result.recommendedActivityId()).isNull();
        assertThat(result.chosenActivityId()).isNull();
        assertThat(result.createdAt()).isNotNull();
        verify(emotionalEventRepository).save(any(EmotionalEvent.class));
    }

    private void thenCreateThrowsBusinessRule() {
        assertThatThrownBy(this::create).isInstanceOf(BusinessRuleException.class);
    }

    private void thenCreateThrowsBusinessRuleContaining(String messageFragment) {
        assertThatThrownBy(this::create)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(messageFragment);
    }

    private void thenDecisionSaved(EmotionalEventResponse result,
                                   RecommendationDecision expectedDecision,
                                   Long expectedChosenActivityId) {
        assertThat(result.recommendationDecision()).isEqualTo(expectedDecision);
        assertThat(result.chosenActivityId()).isEqualTo(expectedChosenActivityId);
    }

    private void thenDecisionThrowsBusinessRule() {
        assertThatThrownBy(this::updateDecision).isInstanceOf(BusinessRuleException.class);
    }

    private void thenDecisionThrowsResourceNotFound() {
        assertThatThrownBy(this::updateDecision).isInstanceOf(ResourceNotFoundException.class);
    }

    private void thenNoPersistenceSideEffects() {
        verify(chatMessageRepository, never()).updateSuggestedActionDecision(any(), any(), any());
        verify(userVectorMemoryService, never()).saveMemory(any());
    }

    private void thenMemorySavedWithFallbackIdsAndChatDecisionSkipped() {
        verify(chatMessageRepository, never()).updateSuggestedActionDecision(any(), any(), any());
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        SaveVectorMemoryCommand command = captor.getValue();
        assertThat(command.sourceId()).isEqualTo(USER_ID.toString());
        assertThat(command.messageId()).isNull();
    }

    private void thenFeedbackSaved(EmotionalEventResponse result, Integer expectedScore, String expectedText) {
        assertThat(result.feedbackScore()).isEqualTo(expectedScore);
        assertThat(result.feedbackText()).isEqualTo(expectedText);
    }

    private void thenFeedbackThrowsBusinessRule() {
        assertThatThrownBy(this::updateFeedback).isInstanceOf(BusinessRuleException.class);
    }

    private void thenFeedbackThrowsResourceNotFound() {
        assertThatThrownBy(this::updateFeedback).isInstanceOf(ResourceNotFoundException.class);
    }
}
