package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.cloudRecommendation.CloudRecommendation;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloud.CreateCloudThoughtUseCase;
import com.huly.backend.domain.useCase.cloud.ListCloudThoughtsUseCase;
import com.huly.backend.domain.useCase.cloud.MarkCloudWorkedOnUseCase;
import com.huly.backend.domain.useCase.cloud.UpdateCloudStatusUseCase;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CloudControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GetCloudRecommendationUseCase getCloudRecommendationUseCase;
    private UserVectorMemoryService userVectorMemoryService;
    private CreateCloudThoughtUseCase createCloudThoughtUseCase;
    private ListCloudThoughtsUseCase listCloudThoughtsUseCase;
    private UpdateCloudStatusUseCase updateCloudStatusUseCase;
    private MarkCloudWorkedOnUseCase markCloudWorkedOnUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getCloudRecommendationUseCase = mock(GetCloudRecommendationUseCase.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);
        createCloudThoughtUseCase = mock(CreateCloudThoughtUseCase.class);
        listCloudThoughtsUseCase = mock(ListCloudThoughtsUseCase.class);
        updateCloudStatusUseCase = mock(UpdateCloudStatusUseCase.class);
        markCloudWorkedOnUseCase = mock(MarkCloudWorkedOnUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        CloudController controller = new CloudController(
                getCloudRecommendationUseCase,
                userVectorMemoryService,
                createCloudThoughtUseCase,
                listCloudThoughtsUseCase,
                updateCloudStatusUseCase,
                markCloudWorkedOnUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── GET /api/clouds ──────────────────────────────────────────────────────

    @Test
    void list_shouldReturn200WithEmptyList_whenNoThoughts() throws Exception {
        when(listCloudThoughtsUseCase.execute(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/clouds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void list_shouldReturn200WithThoughts_whenThoughtsExist() throws Exception {
        CloudThought thought = CloudThought.builder()
                .id(1L).userId(USER_ID).text("me siento ansioso")
                .status(CloudStatus.ACTIVE).workedOn(false).createdAt(Instant.now())
                .build();
        when(listCloudThoughtsUseCase.execute(USER_ID)).thenReturn(List.of(thought));

        mockMvc.perform(get("/api/clouds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("me siento ansioso"))
                .andExpect(jsonPath("$[0].workedOn").value(false));
    }

    // ── POST /api/clouds/thought ─────────────────────────────────────────────

    @Test
    void saveThought_shouldReturn201WithBody_whenThoughtIsValid() throws Exception {
        CloudThought thought = CloudThought.builder()
                .id(1L).userId(USER_ID).text("me siento ansioso")
                .status(CloudStatus.ACTIVE).workedOn(false).createdAt(Instant.now())
                .build();
        when(createCloudThoughtUseCase.execute(eq(USER_ID), eq("me siento ansioso"))).thenReturn(thought);

        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", "me siento ansioso"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("me siento ansioso"));
    }

    @Test
    void saveThought_shouldSaveToVectorMemory_withCorrectUserId() throws Exception {
        CloudThought thought = CloudThought.builder()
                .id(1L).userId(USER_ID).text("me siento ansioso")
                .status(CloudStatus.ACTIVE).workedOn(false).createdAt(Instant.now())
                .build();
        when(createCloudThoughtUseCase.execute(any(), any())).thenReturn(thought);

        mockMvc.perform(post("/api/clouds/thought")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thought", "me siento ansioso"))))
                .andExpect(status().isCreated());

        ArgumentCaptor<SaveVectorMemoryCommand> captor =
                ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().content()).isEqualTo("me siento ansioso");
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

    // ── PATCH /api/clouds/{id}/status ────────────────────────────────────────

    @Test
    void updateStatus_shouldReturn204_whenStatusIsCompleted() throws Exception {
        CloudThought thought = CloudThought.builder()
                .id(1L).userId(USER_ID).text("pensamiento").status(CloudStatus.ACTIVE)
                .workedOn(false).createdAt(Instant.now()).build();
        when(updateCloudStatusUseCase.execute(eq(1L), eq(USER_ID), eq(CloudStatus.COMPLETED))).thenReturn(thought);

        mockMvc.perform(patch("/api/clouds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn204_whenStatusIsCancelled() throws Exception {
        CloudThought thought = CloudThought.builder()
                .id(1L).userId(USER_ID).text("pensamiento").status(CloudStatus.ACTIVE)
                .workedOn(false).createdAt(Instant.now()).build();
        when(updateCloudStatusUseCase.execute(eq(1L), eq(USER_ID), eq(CloudStatus.CANCELLED))).thenReturn(thought);

        mockMvc.perform(patch("/api/clouds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/clouds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INVALID"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusFieldIsMissing() throws Exception {
        mockMvc.perform(patch("/api/clouds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /api/clouds/{id}/worked-on ─────────────────────────────────────

    @Test
    void markWorkedOn_shouldReturn204_whenThoughtExists() throws Exception {
        doNothing().when(markCloudWorkedOnUseCase).execute(1L, USER_ID);

        mockMvc.perform(patch("/api/clouds/1/worked-on"))
                .andExpect(status().isNoContent());

        verify(markCloudWorkedOnUseCase).execute(1L, USER_ID);
    }

    // ── POST /api/clouds/recommendation ──────────────────────────────────────

    @Test
    void getRecommendation_shouldReturn200WithDiaryRecommendation_whenRequestIsValid() throws Exception {
        CloudRecommendation recommendation = new CloudRecommendation(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"
        );
        when(getCloudRecommendationUseCase.execute(any(), eq(USER_ID))).thenReturn(recommendation);

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
        when(getCloudRecommendationUseCase.execute(any(), eq(USER_ID))).thenReturn(
                new CloudRecommendation("diary", "diary", "Título", "Desc.", "/diary"));

        mockMvc.perform(post("/api/clouds/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("thoughts", List.of("no puedo dejar de pensar")))))
                .andExpect(status().isOk());

        verify(getCloudRecommendationUseCase).execute(List.of("no puedo dejar de pensar"), USER_ID);
    }
}
