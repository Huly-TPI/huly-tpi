package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.domain.useCase.emotionalEvent.SaveUserEmotionalStateUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateRequest;
import com.huly.backend.infrastructure.presentation.mapper.emotionalEvent.UserEmotionalStatePresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserEmotionalStateControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SaveUserEmotionalStateUseCase saveUserEmotionalStateUseCase;

    @BeforeEach
    void setUp() {
        saveUserEmotionalStateUseCase = mock(SaveUserEmotionalStateUseCase.class);
        UserEmotionalStateController controller = new UserEmotionalStateController(
                saveUserEmotionalStateUseCase, new UserEmotionalStatePresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

    @Test
    void save_shouldReturn201WithSavedState() throws Exception {
        SaveUserEmotionalStateResponse savedState = new SaveUserEmotionalStateResponse(
                1L, 10L, 0.5, -0.3, 0.2, 0.8, "chatbot", Instant.now());
        when(saveUserEmotionalStateUseCase.execute(any(SaveUserEmotionalStateRequest.class))).thenReturn(savedState);


        UserEmotionalStateRequest request = new UserEmotionalStateRequest(
                10L, 0.5, -0.3, 0.2, 0.8, "chatbot");

                mockMvc.perform(post("/api/emotional-states")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1L))
                        .andExpect(jsonPath("$.userId").value(10L))
                        .andExpect(jsonPath("$.source").value("chatbot"));
    }

    @Test
    void save_shouldReturn400WhenRequestIsInvalid() throws Exception {
        UserEmotionalStateRequest invalidRequest = new UserEmotionalStateRequest(
                null, 0.5, -0.3, 0.2, 0.8, "chatbot");

                mockMvc.perform(post("/api/emotional-states")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void save_shouldReturn400WhenValenceIsOutOfRange() throws Exception {
        UserEmotionalStateRequest invalidRequest = new UserEmotionalStateRequest(
                10L, 1.5, -0.3, 0.2, 0.8, "chatbot");

                mockMvc.perform(post("/api/emotional-states")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void save_shouldReturn400WhenSourceIsBlank() throws Exception {
        UserEmotionalStateRequest invalidRequest = new UserEmotionalStateRequest(
                10L, 0.5, -0.3, 0.2, 0.8, "   ");

                mockMvc.perform(post("/api/emotional-states")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                        .andExpect(status().isBadRequest());
    }

}
