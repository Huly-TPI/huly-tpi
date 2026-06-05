package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.controller.CloudController;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private UserVectorMemoryService userVectorMemoryService;
    private AppUserRepository appUserRepository;

    private static final String USER_EMAIL = "test@huly.com";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getCloudRecommendationUseCase = mock(GetCloudRecommendationUseCase.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);
        appUserRepository = mock(AppUserRepository.class);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(USER_EMAIL);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(appUserRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(AppUserEntity.builder().id(USER_ID).build()));

        CloudController controller = new CloudController(
                getCloudRecommendationUseCase, userVectorMemoryService, appUserRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── /recommendation ──────────────────────────────────────────────────────

    @Test
    void getRecommendation_shouldReturn200WithDiaryRecommendation_whenRequestIsValid() throws Exception {
        CloudRecommendation recommendation = new CloudRecommendation(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"
        );
        when(getCloudRecommendationUseCase.execute(any())).thenReturn(recommendation);

        mockMvc.perform(post("/api/clouds/recommendation")
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
    void getRecommendation_shouldReturn400_whenThoughtsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of()))))
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
    void getRecommendation_shouldDelegateThoughtsToUseCase() throws Exception {
        when(getCloudRecommendationUseCase.execute(any())).thenReturn(
                new CloudRecommendation("diary", "diary", "Título", "Desc.", "/diary"));

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of("no puedo dejar de pensar")))))
                .andExpect(status().isOk());

        verify(getCloudRecommendationUseCase).execute(List.of("no puedo dejar de pensar"));
    }

    // ── /thought ─────────────────────────────────────────────────────────────

    @Test
    void saveThought_shouldReturn204_whenThoughtIsValid() throws Exception {
        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", "me siento ansioso"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void saveThought_shouldCallRememberGuidedCloudInput_withUserId() throws Exception {
        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", "me siento ansioso"))))
                .andExpect(status().isNoContent());

        verify(userVectorMemoryService).rememberGuidedCloudInput(eq(USER_ID), anyString(), eq("me siento ansioso"));
    }

    @Test
    void saveThought_shouldReturn404_whenUserNotFound() throws Exception {
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", "un pensamiento"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveThought_shouldReturn400_whenThoughtIsBlank() throws Exception {
        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveThought_shouldReturn400_whenThoughtFieldIsMissing() throws Exception {
        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
