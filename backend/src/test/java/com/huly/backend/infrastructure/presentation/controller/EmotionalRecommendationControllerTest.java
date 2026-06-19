package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.EmotionalRecommendationItem;
import com.huly.backend.domain.model.EmotionalRecommendationQuery;
import com.huly.backend.domain.model.EmotionalRecommendationResult;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.useCase.emotionalRecommendation.GetEmotionalRecommendationsUseCase;
import com.huly.backend.infrastructure.presentation.dto.emotionalRecommendation.EmotionalRecommendationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
        UserDetails userDetails = new User(
                String.valueOf(AUTHENTICATED_USER_ID),
                "",
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null)
        );
        EmotionalRecommendationController controller = new EmotionalRecommendationController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recommend_shouldReturnOrderedRecommendations() throws Exception {
        EmotionalRecommendationResult result = new EmotionalRecommendationResult(
                List.of(new EmotionalRecommendationItem(
                        1L,
                        ActivityType.RESPIRACION,
                        "Respiracion guiada",
                        "Descripcion",
                        0.92,
                        "Recomendada por arousal alto"
                )),
                false
        );
        when(useCase.execute(any(EmotionalRecommendationQuery.class))).thenReturn(result);

        EmotionalRecommendationRequest request = new EmotionalRecommendationRequest(
                1L,
                EmotionalEventSource.CHATBOT,
                "Estoy ansioso",
                "ANSIEDAD",
                0.91,
                -0.8,
                0.9,
                -0.7,
                0.85,
                "calmarme"
        );

        mockMvc.perform(post("/api/emotional-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallbackUsed").value(false))
                .andExpect(jsonPath("$.recommendations[0].activityId").value(1L))
                .andExpect(jsonPath("$.recommendations[0].type").value("RESPIRACION"));

        ArgumentCaptor<EmotionalRecommendationQuery> queryCaptor =
                ArgumentCaptor.forClass(EmotionalRecommendationQuery.class);
        verify(useCase).execute(queryCaptor.capture());
        assertThat(queryCaptor.getValue().userId()).isEqualTo(AUTHENTICATED_USER_ID);
    }

    @Test
    void recommend_shouldReturn400WhenVadIsInvalid() throws Exception {
        EmotionalRecommendationRequest request = new EmotionalRecommendationRequest(
                1L,
                EmotionalEventSource.CHATBOT,
                "Estoy ansioso",
                "ANSIEDAD",
                0.91,
                -1.5,
                0.9,
                -0.7,
                0.85,
                "calmarme"
        );

        mockMvc.perform(post("/api/emotional-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
