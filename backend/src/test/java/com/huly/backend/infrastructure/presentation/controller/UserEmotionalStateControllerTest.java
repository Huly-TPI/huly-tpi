package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateRequest;
import com.huly.backend.domain.dto.emotionalEvent.SaveUserEmotionalStateResponse;
import com.huly.backend.domain.useCase.emotionalEvent.SaveUserEmotionalStateUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalState.UserEmotionalStateRequest;
import com.huly.backend.infrastructure.presentation.mapper.emotionalEvent.UserEmotionalStatePresentationMapper;
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
    @DisplayName("Devuelve 201 con el estado emocional guardado")
    void saveShouldReturn201WithSavedState() throws Exception {
        // --- arrange ---
        givenSavedState(savedState());
        // --- act ---
        ResultActions result = performSave(validRequest());
        // --- assert ---
        thenCreatedWithSavedState(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la petición es inválida")
    void saveShouldReturn400WhenRequestIsInvalid() throws Exception {
        // --- act ---
        ResultActions result = performSave(requestWithNullUserId());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la valencia está fuera de rango")
    void saveShouldReturn400WhenValenceIsOutOfRange() throws Exception {
        // --- act ---
        ResultActions result = performSave(requestWithValenceOutOfRange());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la fuente está vacía")
    void saveShouldReturn400WhenSourceIsBlank() throws Exception {
        // --- act ---
        ResultActions result = performSave(requestWithBlankSource());
        // --- assert ---
        thenBadRequest(result);
    }

    // --- arrange ---
    private void givenSavedState(SaveUserEmotionalStateResponse response) {
        when(saveUserEmotionalStateUseCase.execute(any(SaveUserEmotionalStateRequest.class))).thenReturn(response);
    }

    private SaveUserEmotionalStateResponse savedState() {
        return new SaveUserEmotionalStateResponse(
                1L, 10L, 0.5, -0.3, 0.2, 0.8, "chatbot", Instant.now());
    }

    private UserEmotionalStateRequest validRequest() {
        return new UserEmotionalStateRequest(10L, 0.5, -0.3, 0.2, 0.8, "chatbot");
    }

    private UserEmotionalStateRequest requestWithNullUserId() {
        return new UserEmotionalStateRequest(null, 0.5, -0.3, 0.2, 0.8, "chatbot");
    }

    private UserEmotionalStateRequest requestWithValenceOutOfRange() {
        return new UserEmotionalStateRequest(10L, 1.5, -0.3, 0.2, 0.8, "chatbot");
    }

    private UserEmotionalStateRequest requestWithBlankSource() {
        return new UserEmotionalStateRequest(10L, 0.5, -0.3, 0.2, 0.8, "   ");
    }

    // --- act ---
    private ResultActions performSave(UserEmotionalStateRequest request) throws Exception {
        return mockMvc.perform(post("/api/emotional-states")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    // --- assert ---
    private void thenCreatedWithSavedState(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.source").value("chatbot"));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }
}
