package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.cloud.CloudThoughtItem;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtRequest;
import com.huly.backend.domain.dto.cloud.CreateCloudThoughtResponse;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsRequest;
import com.huly.backend.domain.dto.cloud.ListCloudThoughtsResponse;
import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnRequest;
import com.huly.backend.domain.dto.cloud.MarkCloudWorkedOnResponse;
import com.huly.backend.domain.dto.cloud.UpdateCloudStatusRequest;
import com.huly.backend.domain.dto.cloud.UpdateCloudStatusResponse;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationRequest;
import com.huly.backend.domain.dto.cloudRecommendation.GetCloudRecommendationResponse;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import com.huly.backend.domain.useCase.cloud.CreateCloudThoughtUseCase;
import com.huly.backend.domain.useCase.cloud.ListCloudThoughtsUseCase;
import com.huly.backend.domain.useCase.cloud.MarkCloudWorkedOnUseCase;
import com.huly.backend.domain.useCase.cloud.UpdateCloudStatusUseCase;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.cloud.CloudPresentationMapper;
import com.huly.backend.infrastructure.presentation.mapper.cloudRecommendation.CloudRecommendationPresentationMapper;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CloudControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GetCloudRecommendationUseCase getCloudRecommendationUseCase;
    private UserVectorMemoryService userVectorMemoryService;
    private CreateCloudThoughtUseCase createCloudThoughtUseCase;
    private ListCloudThoughtsUseCase listCloudThoughtsUseCase;
    private UpdateCloudStatusUseCase updateCloudStatusUseCase;
    private MarkCloudWorkedOnUseCase markCloudWorkedOnUseCase;

    @BeforeEach
    void setUp() {
        getCloudRecommendationUseCase = mock(GetCloudRecommendationUseCase.class);
        userVectorMemoryService = mock(UserVectorMemoryService.class);
        createCloudThoughtUseCase = mock(CreateCloudThoughtUseCase.class);
        listCloudThoughtsUseCase = mock(ListCloudThoughtsUseCase.class);
        updateCloudStatusUseCase = mock(UpdateCloudStatusUseCase.class);
        markCloudWorkedOnUseCase = mock(MarkCloudWorkedOnUseCase.class);

        CloudController controller = new CloudController(
                getCloudRecommendationUseCase,
                userVectorMemoryService,
                createCloudThoughtUseCase,
                listCloudThoughtsUseCase,
                updateCloudStatusUseCase,
                markCloudWorkedOnUseCase,
                new CloudPresentationMapper(),
                new CloudRecommendationPresentationMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── GET /api/clouds ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 200 con una lista vacía cuando el usuario no tiene pensamientos")
    void listShouldReturn200WithEmptyListWhenNoThoughts() throws Exception {
        // --- arrange ---
        givenNoThoughts();
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenOkWithEmptyArray(result);
    }

    @Test
    @DisplayName("Devuelve 200 con los pensamientos cuando existen")
    void listShouldReturn200WithThoughtsWhenThoughtsExist() throws Exception {
        // --- arrange ---
        givenThoughts(List.of(new CloudThoughtItem(1L, "me siento ansioso", false, Instant.now())));
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenOkWithThought(result, 1, "me siento ansioso", false);
    }

    @Test
    @DisplayName("Devuelve 401 al listar cuando no está autenticado")
    void listShouldReturn401WhenNotAuthenticated() throws Exception {
        // --- arrange ---
        givenNoAuthentication();
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenUnauthorized(result);
    }

    // ── POST /api/clouds/thought ─────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 201 con el cuerpo cuando el pensamiento es válido")
    void saveThoughtShouldReturn201WithBodyWhenThoughtIsValid() throws Exception {
        // --- arrange ---
        givenCreatedThought(new CreateCloudThoughtResponse(1L, "me siento ansioso", false, Instant.now()));
        // --- act ---
        ResultActions result = performSaveThought("me siento ansioso");
        // --- assert ---
        thenCreatedWithThought(result, 1, "me siento ansioso");
    }

    @Test
    @DisplayName("Guarda el pensamiento en la memoria vectorial con el usuario correcto")
    void saveThoughtShouldSaveToVectorMemoryWithCorrectUserId() throws Exception {
        // --- arrange ---
        givenCreatedThought(new CreateCloudThoughtResponse(1L, "me siento ansioso", false, Instant.now()));
        // --- act ---
        ResultActions result = performSaveThought("me siento ansioso");
        // --- assert ---
        thenCreated(result);
        thenMemorySavedWith(USER_ID, "me siento ansioso");
    }

    @Test
    @DisplayName("Devuelve 400 cuando el pensamiento está en blanco")
    void saveThoughtShouldReturn400WhenThoughtIsBlank() throws Exception {
        // --- act ---
        ResultActions result = performSaveThought("");
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando falta el campo pensamiento")
    void saveThoughtShouldReturn400WhenThoughtFieldIsMissing() throws Exception {
        // --- act ---
        ResultActions result = performSaveThoughtWithRawBody("{}");
        // --- assert ---
        thenBadRequest(result);
    }

    // ── PATCH /api/clouds/{id}/status ────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 204 cuando el estado es COMPLETED")
    void updateStatusShouldReturn204WhenStatusIsCompleted() throws Exception {
        // --- arrange ---
        givenUpdateStatusReturns(
                new UpdateCloudStatusResponse(1L, "pensamiento", CloudStatus.COMPLETED, false, Instant.now()));
        // --- act ---
        ResultActions result = performUpdateStatus(1, "COMPLETED");
        // --- assert ---
        thenNoContent(result);
    }

    @Test
    @DisplayName("Devuelve 204 cuando el estado es CANCELLED")
    void updateStatusShouldReturn204WhenStatusIsCancelled() throws Exception {
        // --- arrange ---
        givenUpdateStatusReturns(
                new UpdateCloudStatusResponse(1L, "pensamiento", CloudStatus.CANCELLED, false, Instant.now()));
        // --- act ---
        ResultActions result = performUpdateStatus(1, "CANCELLED");
        // --- assert ---
        thenNoContent(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el estado no es un valor válido del enum")
    void updateStatusShouldReturn400WhenStatusIsInvalid() throws Exception {
        // --- act ---
        ResultActions result = performUpdateStatus(1, "INVALID");
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando falta el campo estado")
    void updateStatusShouldReturn400WhenStatusFieldIsMissing() throws Exception {
        // --- act ---
        ResultActions result = performUpdateStatusWithRawBody(1, "{}");
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el caso de uso rechaza la transición de estado")
    void updateStatusShouldReturn400WhenUseCaseRejectsTransition() throws Exception {
        // --- arrange ---
        givenUpdateStatusRejectsWith(new IllegalStateException("Transición no permitida"));
        // --- act ---
        ResultActions result = performUpdateStatus(1, "COMPLETED");
        // --- assert ---
        thenBadRequest(result);
    }

    // ── PATCH /api/clouds/{id}/worked-on ─────────────────────────────────────

    @Test
    @DisplayName("Devuelve 204 y delega en el caso de uso al marcar como trabajado")
    void markWorkedOnShouldReturn204WhenThoughtExists() throws Exception {
        // --- arrange ---
        givenMarkWorkedOnReturns(new MarkCloudWorkedOnResponse(1L));
        // --- act ---
        ResultActions result = performMarkWorkedOn(1);
        // --- assert ---
        thenNoContent(result);
        thenMarkWorkedOnDelegated(1L, USER_ID);
    }

    // ── POST /api/clouds/recommendation ──────────────────────────────────────

    @Test
    @DisplayName("Devuelve 200 con la recomendación de diario cuando el pedido es válido")
    void getRecommendationShouldReturn200WithDiaryRecommendationWhenRequestIsValid() throws Exception {
        // --- arrange ---
        givenRecommendation(new GetCloudRecommendationResponse(
                "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary"));
        // --- act ---
        ResultActions result = performGetRecommendation(List.of("me siento muy triste"));
        // --- assert ---
        thenOkWithRecommendation(result, "diary", "diary", "Escribí en tu diario",
                "Plasmar tus emociones puede ayudarte.", "/diary");
    }

    @Test
    @DisplayName("Devuelve 400 cuando la lista de pensamientos está vacía")
    void getRecommendationShouldReturn400WhenThoughtsIsEmpty() throws Exception {
        // --- act ---
        ResultActions result = performGetRecommendation(List.of());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando falta el campo pensamientos")
    void getRecommendationShouldReturn400WhenThoughtsFieldIsMissing() throws Exception {
        // --- act ---
        ResultActions result = performGetRecommendationWithRawBody("{}");
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Delega los pensamientos y el usuario al caso de uso de recomendación")
    void getRecommendationShouldDelegateThoughtsToUseCase() throws Exception {
        // --- arrange ---
        givenRecommendation(new GetCloudRecommendationResponse("diary", "diary", "Título", "Desc.", "/diary"));
        // --- act ---
        ResultActions result = performGetRecommendation(List.of("no puedo dejar de pensar"));
        // --- assert ---
        thenOk(result);
        thenRecommendationDelegated(List.of("no puedo dejar de pensar"), USER_ID);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenNoThoughts() {
        when(listCloudThoughtsUseCase.execute(any(ListCloudThoughtsRequest.class)))
                .thenReturn(new ListCloudThoughtsResponse(List.of()));
    }

    private void givenThoughts(List<CloudThoughtItem> thoughts) {
        when(listCloudThoughtsUseCase.execute(any(ListCloudThoughtsRequest.class)))
                .thenReturn(new ListCloudThoughtsResponse(thoughts));
    }

    private void givenCreatedThought(CreateCloudThoughtResponse response) {
        when(createCloudThoughtUseCase.execute(any(CreateCloudThoughtRequest.class))).thenReturn(response);
    }

    private void givenUpdateStatusReturns(UpdateCloudStatusResponse response) {
        when(updateCloudStatusUseCase.execute(any(UpdateCloudStatusRequest.class))).thenReturn(response);
    }

    private void givenUpdateStatusRejectsWith(RuntimeException exception) {
        when(updateCloudStatusUseCase.execute(any(UpdateCloudStatusRequest.class))).thenThrow(exception);
    }

    private void givenMarkWorkedOnReturns(MarkCloudWorkedOnResponse response) {
        when(markCloudWorkedOnUseCase.execute(any(MarkCloudWorkedOnRequest.class))).thenReturn(response);
    }

    private void givenRecommendation(GetCloudRecommendationResponse response) {
        when(getCloudRecommendationUseCase.execute(any(GetCloudRecommendationRequest.class))).thenReturn(response);
    }

    // --- act ---
    private ResultActions performList() throws Exception {
        return mockMvc.perform(get("/api/clouds"));
    }

    private ResultActions performSaveThought(String thought) throws Exception {
        return mockMvc.perform(post("/api/clouds/thought")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("thought", thought))));
    }

    private ResultActions performSaveThoughtWithRawBody(String json) throws Exception {
        return mockMvc.perform(post("/api/clouds/thought")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performUpdateStatus(long id, String statusValue) throws Exception {
        return mockMvc.perform(patch("/api/clouds/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", statusValue))));
    }

    private ResultActions performUpdateStatusWithRawBody(long id, String json) throws Exception {
        return mockMvc.perform(patch("/api/clouds/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performMarkWorkedOn(long id) throws Exception {
        return mockMvc.perform(patch("/api/clouds/" + id + "/worked-on"));
    }

    private ResultActions performGetRecommendation(List<String> thoughts) throws Exception {
        return mockMvc.perform(post("/api/clouds/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("thoughts", thoughts))));
    }

    private ResultActions performGetRecommendationWithRawBody(String json) throws Exception {
        return mockMvc.perform(post("/api/clouds/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // --- assert ---
    private void thenOkWithEmptyArray(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    private void thenOkWithThought(ResultActions result, int id, String text, boolean workedOn) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].text").value(text))
                .andExpect(jsonPath("$[0].workedOn").value(workedOn));
    }

    private void thenCreatedWithThought(ResultActions result, int id, String text) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(text));
    }

    private void thenCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    private void thenMemorySavedWith(Long userId, String content) {
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(userId);
        assertThat(captor.getValue().content()).isEqualTo(content);
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenMarkWorkedOnDelegated(Long id, Long userId) {
        ArgumentCaptor<MarkCloudWorkedOnRequest> captor = ArgumentCaptor.forClass(MarkCloudWorkedOnRequest.class);
        verify(markCloudWorkedOnUseCase).execute(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().userId()).isEqualTo(userId);
    }

    private void thenOkWithRecommendation(ResultActions result, String activityType, String actionId,
                                          String title, String description, String redirectUrl) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.activity_type").value(activityType))
                .andExpect(jsonPath("$.action_id").value(actionId))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.redirect_url").value(redirectUrl));
    }

    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenRecommendationDelegated(List<String> thoughts, Long userId) {
        ArgumentCaptor<GetCloudRecommendationRequest> captor =
                ArgumentCaptor.forClass(GetCloudRecommendationRequest.class);
        verify(getCloudRecommendationUseCase).execute(captor.capture());
        assertThat(captor.getValue().thoughts()).isEqualTo(thoughts);
        assertThat(captor.getValue().userId()).isEqualTo(userId);
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
