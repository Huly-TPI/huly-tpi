package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pendingRecommendation.PendingRecommendationResponse;
import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import com.huly.backend.domain.useCase.pending.AddPendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.CompletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.CreatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.DeletePendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.GetPendingTaskUseCase;
import com.huly.backend.domain.useCase.pending.ListPendingTasksUseCase;
import com.huly.backend.domain.useCase.pending.TogglePendingSubtaskUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingPositionUseCase;
import com.huly.backend.domain.useCase.pending.UpdatePendingTaskUseCase;
import com.huly.backend.domain.useCase.pendingRecommendation.GetDailyRecommendationUseCase;
import com.huly.backend.domain.useCase.pendingRecommendation.RespondToRecommendationUseCase;
import com.huly.backend.infrastructure.presentation.dto.pending.AddSubtaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.CreatePendingTaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.RespondToRecommendationRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePendingTaskRequest;
import com.huly.backend.infrastructure.presentation.dto.pending.UpdatePositionRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.pending.PendingPresentationMapper;
import com.huly.backend.infrastructure.presentation.mapper.pending.PendingRecommendationPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PendingControllerTest {

    private MockMvc mockMvc;

    @Mock private CreatePendingTaskUseCase createPendingTaskUseCase;
    @Mock private ListPendingTasksUseCase listPendingTasksUseCase;
    @Mock private GetPendingTaskUseCase getPendingTaskUseCase;
    @Mock private UpdatePendingTaskUseCase updatePendingTaskUseCase;
    @Mock private DeletePendingTaskUseCase deletePendingTaskUseCase;
    @Mock private CompletePendingTaskUseCase completePendingTaskUseCase;
    @Mock private AddPendingSubtaskUseCase addPendingSubtaskUseCase;
    @Mock private TogglePendingSubtaskUseCase togglePendingSubtaskUseCase;
    @Mock private DeletePendingSubtaskUseCase deletePendingSubtaskUseCase;
    @Mock private UpdatePendingPositionUseCase updatePendingPositionUseCase;
    @Mock private GetDailyRecommendationUseCase getDailyRecommendationUseCase;
    @Mock private RespondToRecommendationUseCase respondToRecommendationUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PendingController controller = new PendingController(
                createPendingTaskUseCase,
                listPendingTasksUseCase,
                getPendingTaskUseCase,
                updatePendingTaskUseCase,
                deletePendingTaskUseCase,
                completePendingTaskUseCase,
                addPendingSubtaskUseCase,
                togglePendingSubtaskUseCase,
                deletePendingSubtaskUseCase,
                updatePendingPositionUseCase,
                getDailyRecommendationUseCase,
                respondToRecommendationUseCase,
                new PendingPresentationMapper(),
                new PendingRecommendationPresentationMapper()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs("10");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Crea una tarea pendiente y devuelve HTTP 201 Created")
    void createShouldReturnCreated() throws Exception {
        CreatePendingTaskRequest dto = new CreatePendingTaskRequest("Título", "Desc", null, "FIFTEEN_MIN", "SALUD", List.of());
        PendingTaskResponse response = buildTaskResponse(1L, "Título", EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD);
        givenCreateTaskSucceeds(response);

        ResultActions result = performCreate(dto);

        thenCreatedWithJson(result, 1L, "Título");
    }

    @Test
    @DisplayName("Lista las tareas del usuario filtradas por estado y devuelve HTTP 200 Ok")
    void listShouldReturnOk() throws Exception {
        com.huly.backend.domain.dto.pending.ListPendingTasksResponse response =
                new com.huly.backend.domain.dto.pending.ListPendingTasksResponse(List.of(
                        buildTaskResponse(1L, "Título", EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD)
                ));
        givenListTasksSucceeds(response);

        ResultActions result = performList("PENDING");

        thenOkWithList(result, 1L);
    }

    @Test
    @DisplayName("Obtiene una tarea pendiente por ID y devuelve HTTP 200 Ok")
    void getShouldReturnOk() throws Exception {
        PendingTaskResponse response = buildTaskResponse(1L, "Título", EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD);
        givenGetTaskSucceeds(response);

        ResultActions result = performGet(1L);

        thenOkWithJson(result, 1L);
    }

    @Test
    @DisplayName("Actualiza los campos de una tarea pendiente y devuelve HTTP 204 No Content")
    void updateShouldReturnNoContent() throws Exception {
        UpdatePendingTaskRequest dto = new UpdatePendingTaskRequest("Título", "Desc", null, "FIFTEEN_MIN", "SALUD");

        ResultActions result = performUpdate(1L, dto);

        thenNoContent(result);
        thenUpdateUseCaseCalled();
    }

    @Test
    @DisplayName("Elimina una tarea pendiente y devuelve HTTP 204 No Content")
    void deleteShouldReturnNoContent() throws Exception {
        ResultActions result = performDelete(1L);

        thenNoContent(result);
        thenDeleteUseCaseCalled();
    }

    @Test
    @DisplayName("Completa una tarea pendiente y devuelve HTTP 204 No Content")
    void completeShouldReturnNoContent() throws Exception {
        ResultActions result = performComplete(1L);

        thenNoContent(result);
        thenCompleteUseCaseCalled();
    }

    @Test
    @DisplayName("Agrega una nueva subtarea a una tarea y devuelve HTTP 201 Created")
    void addSubtaskShouldReturnCreated() throws Exception {
        AddSubtaskRequest dto = new AddSubtaskRequest("Sub");
        PendingSubtaskResponse response = new PendingSubtaskResponse(100L, 1L, "Sub", false, 0);
        givenAddSubtaskSucceeds(response);

        ResultActions result = performAddSubtask(1L, dto);

        thenCreatedSubtask(result, 100L, "Sub");
    }

    @Test
    @DisplayName("Alterna el estado completado de una subtarea y devuelve HTTP 204 No Content")
    void toggleSubtaskShouldReturnNoContent() throws Exception {
        ResultActions result = performToggleSubtask(1L, 100L);

        thenNoContent(result);
        thenToggleSubtaskUseCaseCalled();
    }

    @Test
    @DisplayName("Elimina una subtarea y devuelve HTTP 204 No Content")
    void deleteSubtaskShouldReturnNoContent() throws Exception {
        ResultActions result = performDeleteSubtask(1L, 100L);

        thenNoContent(result);
        thenDeleteSubtaskUseCaseCalled();
    }

    @Test
    @DisplayName("Actualiza las coordenadas de arrastre (drag-and-drop) de una tarea y devuelve HTTP 200 Ok")
    void updatePositionShouldReturnOk() throws Exception {
        UpdatePositionRequest dto = new UpdatePositionRequest(10.0, 20.0);
        PendingTaskResponse response = buildTaskResponseWithCoords(1L, "Título", 10.0, 20.0);
        givenUpdatePositionSucceeds(response);

        ResultActions result = performUpdatePosition(1L, dto);

        thenOkWithPosition(result, 10.0, 20.0);
    }

    @Test
    @DisplayName("Obtiene la recomendación diaria de hoy y devuelve HTTP 200 Ok")
    void getTodayRecommendationShouldReturnOk() throws Exception {
        PendingRecommendationResponse response = buildRecommendationResponse(1L, RecommendationResponseDecision.PENDING, true, true);
        givenGetRecommendationSucceeds(response);

        ResultActions result = performGetTodayRecommendation();

        thenOkWithRecommendation(result, 1L, true);
    }

    @Test
    @DisplayName("Devuelve HTTP 204 No Content si la recomendación diaria de hoy no es aplicable por falta de tareas")
    void getTodayRecommendationShouldReturnNoContentWhenNotApplicable() throws Exception {
        PendingRecommendationResponse response = buildRecommendationResponse(null, null, false, false);
        givenGetRecommendationSucceeds(response);

        ResultActions result = performGetTodayRecommendation();

        thenNoContent(result);
    }

    @Test
    @DisplayName("Genera de forma forzada una nueva recomendación diaria y devuelve HTTP 200 Ok")
    void generateRecommendationShouldReturnOk() throws Exception {
        PendingRecommendationResponse response = buildRecommendationResponse(1L, RecommendationResponseDecision.PENDING, true, true);
        givenGetRecommendationSucceeds(response);

        ResultActions result = performGenerateRecommendation();

        thenOkWithRecommendation(result, 1L, true);
    }

    @Test
    @DisplayName("Devuelve HTTP 204 No Content si se fuerza la generación pero no es aplicable por falta de tareas")
    void generateRecommendationShouldReturnNoContentWhenNotApplicable() throws Exception {
        PendingRecommendationResponse response = buildRecommendationResponse(null, null, false, false);
        givenGetRecommendationSucceeds(response);

        ResultActions result = performGenerateRecommendation();

        thenNoContent(result);
    }

    @Test
    @DisplayName("Guarda la respuesta de aceptación o rechazo a una recomendación diaria y devuelve HTTP 200 Ok")
    void respondToRecommendationShouldReturnOk() throws Exception {
        RespondToRecommendationRequest dto = new RespondToRecommendationRequest("ACCEPTED");
        PendingRecommendationResponse response = buildRecommendationResponse(1L, RecommendationResponseDecision.ACCEPTED, false, true);
        givenRespondToRecommendationSucceeds(response);

        ResultActions result = performRespondToRecommendation(1L, dto);

        thenOkWithRespond(result, 1L, "ACCEPTED");
    }

    // --- arrange ---

    private void authenticateAs(String userId) {
        UserDetails userDetails = new User(userId, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenCreateTaskSucceeds(PendingTaskResponse response) {
        when(createPendingTaskUseCase.execute(any())).thenReturn(response);
    }

    private void givenListTasksSucceeds(com.huly.backend.domain.dto.pending.ListPendingTasksResponse response) {
        when(listPendingTasksUseCase.execute(any())).thenReturn(response);
    }

    private void givenGetTaskSucceeds(PendingTaskResponse response) {
        when(getPendingTaskUseCase.execute(any())).thenReturn(response);
    }

    private void givenAddSubtaskSucceeds(PendingSubtaskResponse response) {
        when(addPendingSubtaskUseCase.execute(any())).thenReturn(response);
    }

    private void givenUpdatePositionSucceeds(PendingTaskResponse response) {
        when(updatePendingPositionUseCase.execute(any())).thenReturn(response);
    }

    private void givenGetRecommendationSucceeds(PendingRecommendationResponse response) {
        when(getDailyRecommendationUseCase.execute(any())).thenReturn(response);
    }

    private void givenRespondToRecommendationSucceeds(PendingRecommendationResponse response) {
        when(respondToRecommendationUseCase.execute(any())).thenReturn(response);
    }

    // --- act ---

    private ResultActions performCreate(CreatePendingTaskRequest dto) throws Exception {
        return mockMvc.perform(post("/api/pending")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions performList(String status) throws Exception {
        return mockMvc.perform(get("/api/pending").param("status", status));
    }

    private ResultActions performGet(Long id) throws Exception {
        return mockMvc.perform(get("/api/pending/" + id));
    }

    private ResultActions performUpdate(Long id, UpdatePendingTaskRequest dto) throws Exception {
        return mockMvc.perform(patch("/api/pending/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions performDelete(Long id) throws Exception {
        return mockMvc.perform(delete("/api/pending/" + id));
    }

    private ResultActions performComplete(Long id) throws Exception {
        return mockMvc.perform(patch("/api/pending/" + id + "/complete"));
    }

    private ResultActions performAddSubtask(Long id, AddSubtaskRequest dto) throws Exception {
        return mockMvc.perform(post("/api/pending/" + id + "/subtasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions performToggleSubtask(Long taskId, Long subtaskId) throws Exception {
        return mockMvc.perform(patch("/api/pending/" + taskId + "/subtasks/" + subtaskId + "/toggle"));
    }

    private ResultActions performDeleteSubtask(Long taskId, Long subtaskId) throws Exception {
        return mockMvc.perform(delete("/api/pending/" + taskId + "/subtasks/" + subtaskId));
    }

    private ResultActions performUpdatePosition(Long id, UpdatePositionRequest dto) throws Exception {
        return mockMvc.perform(patch("/api/pending/" + id + "/position")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions performGetTodayRecommendation() throws Exception {
        return mockMvc.perform(get("/api/pending/recommendation/today"));
    }

    private ResultActions performGenerateRecommendation() throws Exception {
        return mockMvc.perform(post("/api/pending/recommendation/generate"));
    }

    private ResultActions performRespondToRecommendation(Long id, RespondToRecommendationRequest dto) throws Exception {
        return mockMvc.perform(post("/api/pending/recommendation/" + id + "/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    // --- assert ---

    private void thenCreatedWithJson(ResultActions result, Long id, String title) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title));
    }

    private void thenOkWithList(ResultActions result, Long firstId) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(firstId));
    }

    private void thenOkWithJson(ResultActions result, Long id) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenUpdateUseCaseCalled() {
        verify(updatePendingTaskUseCase).execute(any());
    }

    private void thenDeleteUseCaseCalled() {
        verify(deletePendingTaskUseCase).execute(any());
    }

    private void thenCompleteUseCaseCalled() {
        verify(completePendingTaskUseCase).execute(any());
    }

    private void thenCreatedSubtask(ResultActions result, Long subtaskId, String text) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(subtaskId))
                .andExpect(jsonPath("$.text").value(text));
    }

    private void thenToggleSubtaskUseCaseCalled() {
        verify(togglePendingSubtaskUseCase).execute(any());
    }

    private void thenDeleteSubtaskUseCaseCalled() {
        verify(deletePendingSubtaskUseCase).execute(any());
    }

    private void thenOkWithPosition(ResultActions result, double x, double y) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.positionX").value(x))
                .andExpect(jsonPath("$.positionY").value(y));
    }

    private void thenOkWithRecommendation(ResultActions result, Long id, boolean isNew) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationId").value(id))
                .andExpect(jsonPath("$.isNew").value(isNew));
    }

    private void thenOkWithRespond(ResultActions result, Long id, String decision) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationId").value(id))
                .andExpect(jsonPath("$.decision").value(decision));
    }

    // --- helpers ---

    private PendingTaskResponse buildTaskResponse(Long id, String title, EstimatedDuration duration, PendingCategory category) {
        return new PendingTaskResponse(
                id, title, "Desc", null, duration, category,
                PendingStatus.PENDING, List.of(), null, null, null, null, true, Instant.now(), null
        );
    }

    private PendingTaskResponse buildTaskResponseWithCoords(Long id, String title, double x, double y) {
        return new PendingTaskResponse(
                id, title, "Desc", null, EstimatedDuration.FIFTEEN_MIN, PendingCategory.SALUD,
                PendingStatus.PENDING, List.of(), x, y, 4.0, Instant.now(), false, Instant.now(), null
        );
    }

    private PendingRecommendationResponse buildRecommendationResponse(Long id, RecommendationResponseDecision decision, boolean isNew, boolean applicable) {
        return new PendingRecommendationResponse(id, LocalDate.now(), decision, List.of(10L), isNew, applicable);
    }
}
