package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.useCase.GetCloudRecommendationUseCase;
import com.huly.backend.presentation.dto.CloudRecommendationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CloudControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GetCloudRecommendationUseCase getCloudRecommendationUseCase;

    @BeforeEach
    void setUp() {
        getCloudRecommendationUseCase = mock(GetCloudRecommendationUseCase.class);
        CloudController controller = new CloudController(getCloudRecommendationUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getRecommendation_shouldReturn200WithDiaryRecommendation_whenRequestIsValid() throws Exception {
        CloudRecommendation recommendation = new CloudRecommendation(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"
        );
        when(getCloudRecommendationUseCase.execute(any())).thenReturn(recommendation);

        CloudRecommendationRequest request = new CloudRecommendationRequest(List.of("me siento muy triste"));

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_type").value("diary"))
                .andExpect(jsonPath("$.action_id").value("diary"))
                .andExpect(jsonPath("$.title").value("Escribí en tu diario"))
                .andExpect(jsonPath("$.description").value("Plasmar tus emociones puede ayudarte."))
                .andExpect(jsonPath("$.redirect_url").value("/diary"));
    }

    @Test
    void getRecommendation_shouldReturn200WithCloudsRecommendation() throws Exception {
        CloudRecommendation recommendation = new CloudRecommendation(
                "clouds", "clouds", "Soltar más pensamientos",
                "Seguí liberando lo que sentís.", "/clouds"
        );
        when(getCloudRecommendationUseCase.execute(any())).thenReturn(recommendation);

        CloudRecommendationRequest request = new CloudRecommendationRequest(List.of("hoy fue un día tranquilo"));

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_type").value("clouds"))
                .andExpect(jsonPath("$.action_id").value("clouds"))
                .andExpect(jsonPath("$.redirect_url").value("/clouds"));
    }

    @Test
    void getRecommendation_shouldReturn400_whenThoughtsIsEmpty() throws Exception {
        CloudRecommendationRequest request = new CloudRecommendationRequest(List.of());

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendation_shouldReturn400_whenThoughtsFieldIsMissing() throws Exception {
        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendation_shouldDelegateThoughtToUseCase() throws Exception {
        CloudRecommendation recommendation = new CloudRecommendation(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"
        );
        when(getCloudRecommendationUseCase.execute(any())).thenReturn(recommendation);

        CloudRecommendationRequest request = new CloudRecommendationRequest(List.of("no puedo dejar de pensar en lo que pasó"));

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(getCloudRecommendationUseCase).execute(List.of("no puedo dejar de pensar en lo que pasó"));
    }
}
