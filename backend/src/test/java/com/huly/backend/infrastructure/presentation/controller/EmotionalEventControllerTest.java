package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.emotionalEvent.CreateEmotionalEventRequest;
import com.huly.backend.domain.dto.emotionalEvent.EmotionalEventResponse;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventDecisionRequest;
import com.huly.backend.domain.dto.emotionalEvent.UpdateEmotionalEventFeedbackRequest;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventRequest;
import com.huly.backend.infrastructure.presentation.mapper.emotionalEvent.EmotionalEventPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmotionalEventControllerTest {

    private static final Long EVENT_ID = 10L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreateEmotionalEventUseCase createUseCase;
    private UpdateEmotionalEventDecisionUseCase decisionUseCase;
    private UpdateEmotionalEventFeedbackUseCase feedbackUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateEmotionalEventUseCase.class);
        decisionUseCase = mock(UpdateEmotionalEventDecisionUseCase.class);
        feedbackUseCase = mock(UpdateEmotionalEventFeedbackUseCase.class);
        EmotionalEventController controller = new EmotionalEventController(
                createUseCase, decisionUseCase, feedbackUseCase, new EmotionalEventPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve 201 con el evento emocional creado")
    void createShouldReturn201WithCreatedEvent() throws Exception {
        // --- arrange ---
        givenCreatedEvent(eventResponse());
        // --- act ---
        ResultActions result = performCreate(createRequest());
        // --- assert ---
        thenCreatedWithEvent(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la emoción detectada está vacía")
    void createShouldReturn400WhenDetectedEmotionIsBlank() throws Exception {
        // --- act ---
        ResultActions result = performCreate(blankEmotionRequest());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 200 con el evento actualizado tras la decisión")
    void updateDecisionShouldReturnUpdatedEvent() throws Exception {
        // --- arrange ---
        givenUpdatedDecision(RecommendationDecision.ACCEPTED, 1L);
        // --- act ---
        ResultActions result = performUpdateDecision(EVENT_ID, decisionRequest(RecommendationDecision.ACCEPTED, null));
        // --- assert ---
        thenOkWithDecision(result);
    }

    @Test
    @DisplayName("Devuelve 200 con el evento actualizado tras el feedback")
    void updateFeedbackShouldReturnUpdatedEvent() throws Exception {
        // --- arrange ---
        givenUpdatedFeedback(4, "Me siento mejor");
        // --- act ---
        ResultActions result = performUpdateFeedback(EVENT_ID, feedbackRequest(4, "Me siento mejor"));
        // --- assert ---
        thenOkWithFeedback(result);
    }

    // --- arrange ---
    private void givenCreatedEvent(EmotionalEventResponse response) {
        when(createUseCase.execute(any(CreateEmotionalEventRequest.class))).thenReturn(response);
    }

    private void givenUpdatedDecision(RecommendationDecision decision, Long chosenActivityId) {
        when(decisionUseCase.execute(any(UpdateEmotionalEventDecisionRequest.class)))
                .thenReturn(eventResponseWithDecision(decision, chosenActivityId));
    }

    private void givenUpdatedFeedback(Integer feedbackScore, String feedbackText) {
        when(feedbackUseCase.execute(any(UpdateEmotionalEventFeedbackRequest.class)))
                .thenReturn(eventResponseWithFeedback(feedbackScore, feedbackText));
    }

    private EmotionalEventRequest createRequest() {
        return new EmotionalEventRequest(
                1L,
                EmotionalEventSource.CHATBOT,
                "Estoy muy ansioso",
                "ANSIEDAD",
                0.91,
                -0.8,
                0.9,
                -0.7,
                0.85,
                "calmarme",
                "Respira",
                1L,
                null
        );
    }

    private EmotionalEventRequest blankEmotionRequest() {
        return new EmotionalEventRequest(
                1L, EmotionalEventSource.CHATBOT, "texto", " ",
                0.8, -0.2, 0.2, 0.1, 0.5,
                null, null, null, null
        );
    }

    private EmotionalEventDecisionRequest decisionRequest(RecommendationDecision decision, Long chosenActivityId) {
        return new EmotionalEventDecisionRequest(decision, chosenActivityId);
    }

    private EmotionalEventFeedbackRequest feedbackRequest(Integer feedbackScore, String feedbackText) {
        return new EmotionalEventFeedbackRequest(feedbackScore, feedbackText);
    }

    private EmotionalEventResponse eventResponse() {
        Instant now = Instant.now();
        return new EmotionalEventResponse(
                10L,
                1L,
                EmotionalEventSource.CHATBOT,
                "Estoy muy ansioso",
                "ANSIEDAD",
                0.91,
                -0.8,
                0.9,
                -0.7,
                0.85,
                "calmarme",
                "Respira",
                1L,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    private EmotionalEventResponse eventResponseWithDecision(RecommendationDecision decision, Long chosenActivityId) {
        EmotionalEventResponse base = eventResponse();
        return new EmotionalEventResponse(
                base.id(), base.userId(), base.source(), base.inputText(), base.detectedEmotion(),
                base.confidence(), base.valence(), base.arousal(), base.dominance(), base.intensity(),
                base.userGoal(), base.generatedRecommendation(), base.recommendedActivityId(),
                chosenActivityId, decision, base.feedbackScore(), base.feedbackText(),
                base.createdAt(), base.updatedAt()
        );
    }

    private EmotionalEventResponse eventResponseWithFeedback(Integer feedbackScore, String feedbackText) {
        EmotionalEventResponse base = eventResponse();
        return new EmotionalEventResponse(
                base.id(), base.userId(), base.source(), base.inputText(), base.detectedEmotion(),
                base.confidence(), base.valence(), base.arousal(), base.dominance(), base.intensity(),
                base.userGoal(), base.generatedRecommendation(), base.recommendedActivityId(),
                base.chosenActivityId(), base.recommendationDecision(), feedbackScore, feedbackText,
                base.createdAt(), base.updatedAt()
        );
    }

    // --- act ---
    private ResultActions performCreate(EmotionalEventRequest request) throws Exception {
        return mockMvc.perform(post("/api/emotional-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performUpdateDecision(Long id, EmotionalEventDecisionRequest request) throws Exception {
        return mockMvc.perform(patch("/api/emotional-events/" + id + "/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performUpdateFeedback(Long id, EmotionalEventFeedbackRequest request) throws Exception {
        return mockMvc.perform(patch("/api/emotional-events/" + id + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    // --- assert ---
    private void thenCreatedWithEvent(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.source").value("CHATBOT"))
                .andExpect(jsonPath("$.detectedEmotion").value("ANSIEDAD"));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenOkWithDecision(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationDecision").value("ACCEPTED"))
                .andExpect(jsonPath("$.chosenActivityId").value(1L));
    }

    private void thenOkWithFeedback(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackScore").value(4))
                .andExpect(jsonPath("$.feedbackText").value("Me siento mejor"));
    }
}
