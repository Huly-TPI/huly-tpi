package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.CreateEmotionalEventCommand;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UpdateEmotionalEventFeedbackCommand;
import com.huly.backend.domain.model.UpdateRecommendationDecisionCommand;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.useCase.emotionalEvent.CreateEmotionalEventUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventDecisionUseCase;
import com.huly.backend.domain.useCase.emotionalEvent.UpdateEmotionalEventFeedbackUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventDecisionRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventFeedbackRequest;
import com.huly.backend.infrastructure.presentation.dto.emotionalEvent.EmotionalEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
        EmotionalEventController controller = new EmotionalEventController(createUseCase, decisionUseCase, feedbackUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldReturn201WithCreatedEvent() throws Exception {
        when(createUseCase.execute(any(CreateEmotionalEventCommand.class))).thenReturn(event());
        EmotionalEventRequest request = new EmotionalEventRequest(
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

        mockMvc.perform(post("/api/emotional-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.source").value("CHATBOT"))
                .andExpect(jsonPath("$.detectedEmotion").value("ANSIEDAD"));
    }

    @Test
    void create_shouldReturn400WhenDetectedEmotionIsBlank() throws Exception {
        EmotionalEventRequest request = new EmotionalEventRequest(
                1L, EmotionalEventSource.CHATBOT, "texto", " ",
                0.8, -0.2, 0.2, 0.1, 0.5,
                null, null, null, null
        );

        mockMvc.perform(post("/api/emotional-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDecision_shouldReturnUpdatedEvent() throws Exception {
        EmotionalEvent updated = event().toBuilder()
                .recommendationDecision(RecommendationDecision.ACCEPTED)
                .chosenActivityId(1L)
                .build();
        when(decisionUseCase.execute(any(), any(UpdateRecommendationDecisionCommand.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/emotional-events/10/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EmotionalEventDecisionRequest(RecommendationDecision.ACCEPTED, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationDecision").value("ACCEPTED"))
                .andExpect(jsonPath("$.chosenActivityId").value(1L));
    }

    @Test
    void updateFeedback_shouldReturnUpdatedEvent() throws Exception {
        EmotionalEvent updated = event().toBuilder()
                .feedbackScore(4)
                .feedbackText("Me siento mejor")
                .build();
        when(feedbackUseCase.execute(any(), any(UpdateEmotionalEventFeedbackCommand.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/emotional-events/10/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EmotionalEventFeedbackRequest(4, "Me siento mejor"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackScore").value(4))
                .andExpect(jsonPath("$.feedbackText").value("Me siento mejor"));
    }

    private EmotionalEvent event() {
        Instant now = Instant.now();
        return EmotionalEvent.builder()
                .id(10L)
                .userId(1L)
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
                .recommendedActivityId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
