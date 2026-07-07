package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.emotionalRecommendation.EmotionalRecommendationItem;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsRequest;
import com.huly.backend.domain.dto.emotionalRecommendation.GetEmotionalRecommendationsResponse;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import com.huly.backend.infrastructure.presentation.mapper.emotionalRecommendation.EmotionalRecommendationPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmotionalRecommendationControllerTest {

    private static final Long AUTHENTICATED_USER_ID = 7L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GetEmotionalRecommendationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = mock(GetEmotionalRecommendationsUseCase.class);
        EmotionalRecommendationController controller = new EmotionalRecommendationController(
                useCase, new EmotionalRecommendationPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        authenticateAs(String.valueOf(AUTHENTICATED_USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 con las recomendaciones ordenadas del usuario autenticado")
    void recommendShouldReturnOrderedRecommendations() throws Exception {
        // --- arrange ---
        givenRecommendations(orderedRecommendations());
        // --- act ---
        ResultActions result = performRecommend(validRequest());
        // --- assert ---
        thenOkWithOrderedRecommendations(result);
        thenUseCaseCalledWithAuthenticatedUserId();
    }

    @Test
    @DisplayName("Devuelve 400 cuando algún valor VAD es inválido")
    void recommendShouldReturn400WhenVadIsInvalid() throws Exception {
        // --- act ---
        ResultActions result = performRecommend(invalidVadRequest());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 401 cuando no está autenticado")
    void recommendShouldReturn401WhenNotAuthenticated() throws Exception {
        // --- arrange ---
        givenNoAuthentication();
        // --- act ---
        ResultActions result = performRecommend(validRequest());
        // --- assert ---
        thenUnauthorized(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenRecommendations(GetEmotionalRecommendationsResponse response) {
        when(useCase.execute(any(GetEmotionalRecommendationsRequest.class))).thenReturn(response);
    }

    private GetEmotionalRecommendationsResponse orderedRecommendations() {
        return new GetEmotionalRecommendationsResponse(
                List.of(new EmotionalRecommendationItem(
                        1L,
                        ActivityType.BREATHING,
                        "Respiracion guiada",
                        "Descripcion",
                        0.92,
                        "Recomendada por arousal alto",
                        "/guided-breathing"
                )),
                false
        );
    }

    private EmotionalRecommendationRequest validRequest() {
        return new EmotionalRecommendationRequest(
                1L, EmotionalEventSource.CHATBOT, "Estoy ansioso", "ANSIEDAD",
                0.91, -0.8, 0.9, -0.7, 0.85, "calmarme");
    }

    private EmotionalRecommendationRequest invalidVadRequest() {
        return new EmotionalRecommendationRequest(
                1L, EmotionalEventSource.CHATBOT, "Estoy ansioso", "ANSIEDAD",
                0.91, -1.5, 0.9, -0.7, 0.85, "calmarme");
    }

    // --- act ---
    private ResultActions performRecommend(EmotionalRecommendationRequest request) throws Exception {
        return mockMvc.perform(post("/api/emotional-recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    // --- assert ---
    private void thenOkWithOrderedRecommendations(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.recommendations[0].activityId").value(1L))
                .andExpect(jsonPath("$.recommendations[0].type").value("BREATHING"));
    }

    private void thenUseCaseCalledWithAuthenticatedUserId() {
        ArgumentCaptor<GetEmotionalRecommendationsRequest> queryCaptor =
                ArgumentCaptor.forClass(GetEmotionalRecommendationsRequest.class);
        verify(useCase).execute(queryCaptor.capture());
        assertThat(queryCaptor.getValue().userId()).isEqualTo(AUTHENTICATED_USER_ID);
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
