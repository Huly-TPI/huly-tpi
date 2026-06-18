package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.LanternRecommendation;
import com.huly.backend.domain.useCase.lanternRecommendation.GetLanternRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.controller.LanternRecommendationController;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LanternRecommendationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GetLanternRecommendationUseCase getLanternRecommendationUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getLanternRecommendationUseCase = mock(GetLanternRecommendationUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        LanternRecommendationController controller = new LanternRecommendationController(getLanternRecommendationUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRecommendation_shouldReturn200WithDiaryRecommendation_whenRequestIsValid() throws Exception {
        LanternRecommendation recommendation = new LanternRecommendation(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"
        );
        when(getLanternRecommendationUseCase.execute(any(), eq(USER_ID))).thenReturn(recommendation);

        mockMvc.perform(post("/api/lanterns/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of("me siento muy triste")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_type").value("diary"))
                .andExpect(jsonPath("$.action_id").value("diary"))
                .andExpect(jsonPath("$.title").value("Escribí en tu diario"))
                .andExpect(jsonPath("$.description").value("Plasmar tus emociones puede ayudarte."))
                .andExpect(jsonPath("$.redirect_url").value("/diary"));
    }

    @Test
    void getRecommendation_shouldReturn200WithLanternsRecommendation_whenNubeActivityReturned() throws Exception {
        LanternRecommendation recommendation = new LanternRecommendation(
                "lanterns", "lanterns", "Faroles emocionales",
                "Soltá tus pensamientos en un farolito.", "/lanterns"
        );
        when(getLanternRecommendationUseCase.execute(any(), eq(USER_ID))).thenReturn(recommendation);

        mockMvc.perform(post("/api/lanterns/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of("quiero soltar esto")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_type").value("lanterns"))
                .andExpect(jsonPath("$.redirect_url").value("/lanterns"));
    }

    @Test
    void getRecommendation_shouldReturn400_whenThoughtsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/lanterns/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendation_shouldReturn400_whenThoughtsFieldIsMissing() throws Exception {
        mockMvc.perform(post("/api/lanterns/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendation_shouldDelegateThoughtsToUseCase() throws Exception {
        when(getLanternRecommendationUseCase.execute(any(), eq(USER_ID))).thenReturn(
                new LanternRecommendation("diary", "diary", "Título", "Desc.", "/diary"));

        mockMvc.perform(post("/api/lanterns/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of("no puedo dejar de pensar")))))
                .andExpect(status().isOk());

        verify(getLanternRecommendationUseCase).execute(List.of("no puedo dejar de pensar"), USER_ID);
    }
}
