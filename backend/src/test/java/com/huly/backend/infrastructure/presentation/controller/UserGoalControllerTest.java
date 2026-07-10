package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userGoal.UserGoalPage;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.useCase.userGoal.*;
import com.huly.backend.infrastructure.presentation.dto.userGoal.AcceptChallengeRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest;
import com.huly.backend.infrastructure.presentation.mapper.userGoal.UserGoalPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserGoalControllerTest {

    private static final Long USER_ID = 10L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AddUserGoalUseCase addUserGoalUseCase;
    private GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;
    private DeleteUserGoalUseCase deleteUserGoalUseCase;
    private UpdateUserGoalUseCase updateUserGoalUseCase;
    private CompleteUserGoalUseCase completeUserGoalUseCase;
    private AcceptChallengeUseCase acceptChallengeUseCase;

    @BeforeEach
    void setUp() {
        addUserGoalUseCase = mock(AddUserGoalUseCase.class);
        getUserGoalsByUserUseCase = mock(GetUserGoalsByUserUseCase.class);
        deleteUserGoalUseCase = mock(DeleteUserGoalUseCase.class);
        updateUserGoalUseCase = mock(UpdateUserGoalUseCase.class);
        completeUserGoalUseCase = mock(CompleteUserGoalUseCase.class);
        acceptChallengeUseCase = mock(AcceptChallengeUseCase.class);

        UserGoalController controller = new UserGoalController(acceptChallengeUseCase,
                addUserGoalUseCase, getUserGoalsByUserUseCase,
                deleteUserGoalUseCase, updateUserGoalUseCase, completeUserGoalUseCase,
                new UserGoalPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 201 al aceptar un reto con datos válidos")
    void acceptChallengeShouldReturn201WhenRequestIsValid() throws Exception {
        givenAcceptChallengeSucceeds();

        ResultActions result = performAcceptChallenge("Reto", "Desc", 2L);

        thenCreatedWithGoal(result, 1L, "Reto", "PENDING");
    }

    @Test
    @DisplayName("Devuelve 201 al agregar una meta con datos válidos")
    void addShouldReturn201WhenRequestIsValid() throws Exception {
        givenAddSucceedsForValidRequest();

        ResultActions result = performAdd("Respirar", "Desc", 2L);

        thenCreatedWithGoal(result, 1L, "Respirar", "PENDING");
    }

    @Test
    @DisplayName("Devuelve 201 al agregar una meta con activityId nulo")
    void addShouldReturn201WithNullActivityId() throws Exception {
        givenAddSucceedsForNullFields();

        ResultActions result = performAdd("Respirar", null, null);

        thenCreated(result);
    }

    @Test
    @DisplayName("Devuelve 400 al agregar una meta con título vacío")
    void addShouldReturn400WhenTitleIsBlank() throws Exception {
        ResultActions result = performAdd("", null, null);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 401 al agregar una meta sin estar autenticado")
    void addShouldReturn401WhenNotAuthenticated() throws Exception {
        givenNoAuthentication();

        ResultActions result = performAdd("Titulo", null, null);

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 200 con metas completadas y pendientes paginadas")
    void listByUserShouldReturn200WithPaginatedCompletadosAndPendientes() throws Exception {
        givenGoalsPage();

        ResultActions result = performListByUser();

        thenOkWithPaginatedGoals(result);
    }

    @Test
    @DisplayName("Usa los parámetros size y page al listar las metas")
    void listByUserShouldUseSizeAndPageParams() throws Exception {
        givenGoalsPageForParams();

        ResultActions result = performListByUser(1, 3);

        thenOkWithPageParams(result);
    }

    @Test
    @DisplayName("Devuelve 200 con páginas vacías cuando no hay metas")
    void listByUserShouldReturn200WithEmptyPagesWhenNoGoals() throws Exception {
        givenEmptyGoalsPage();

        ResultActions result = performListByUser();

        thenOkWithEmptyPages(result);
    }

    @Test
    @DisplayName("Devuelve 200 al actualizar una meta con datos válidos")
    void updateShouldReturn200WhenRequestIsValid() throws Exception {
        givenUpdateSucceeds();

        ResultActions result = performUpdate(1L, "Nuevo", "Desc", 2L);

        thenOkWithUpdatedGoal(result, 1L, "Nuevo");
    }

    @Test
    @DisplayName("Devuelve 400 al actualizar una meta con título vacío")
    void updateShouldReturn400WhenTitleIsBlank() throws Exception {
        ResultActions result = performUpdate(1L, "", null, null);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 204 al eliminar una meta existente")
    void deleteShouldReturn204WhenGoalExists() throws Exception {
        ResultActions result = performDelete(1L);

        thenNoContent(result);
        thenDeleteWasCalledWith(1L);
    }

    @Test
    @DisplayName("Devuelve 200 con estado completado cuando la meta existe")
    void completeShouldReturn200WithCompletedStatusWhenGoalExists() throws Exception {
        givenCompleteSucceeds(1L);

        ResultActions result = performComplete(1L);

        thenOkWithCompletedGoal(result, 1L);
    }

    @Test
    @DisplayName("Delega en el caso de uso con el id correcto al completar")
    void completeShouldDelegateToUseCaseWithCorrectId() throws Exception {
        givenCompleteSucceeds(42L);

        ResultActions result = performComplete(42L);

        thenOk(result);
        thenCompleteWasCalledWith(42L);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenAcceptChallengeSucceeds() {
        when(acceptChallengeUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && "Reto".equals(r.title())
                        && "Desc".equals(r.description()) && Long.valueOf(2L).equals(r.activityId()))))
                .thenReturn(new AcceptChallengeResponse(goalItem(1L, "Reto", "PENDING")));
    }

    private void givenAddSucceedsForValidRequest() {
        when(addUserGoalUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && "Respirar".equals(r.title())
                        && "Desc".equals(r.description()) && Long.valueOf(2L).equals(r.activityId()))))
                .thenReturn(new AddUserGoalResponse(goalItem(1L, "Respirar", "PENDING")));
    }

    private void givenAddSucceedsForNullFields() {
        when(addUserGoalUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && r.description() == null && r.activityId() == null)))
                .thenReturn(new AddUserGoalResponse(goalItem(1L, "Respirar", "PENDING")));
    }

    private void givenGoalsPage() {
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(goalItem(1L, "Completado", "COMPLETED")), 0, 5, 1),
                page(List.of(goalItem(2L, "Pendiente", "PENDING")), 0, 5, 1));
        when(getUserGoalsByUserUseCase.execute(any(GetUserGoalsRequest.class))).thenReturn(response);
    }

    private void givenGoalsPageForParams() {
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(), 1, 3, 0),
                page(List.of(), 1, 3, 0));
        when(getUserGoalsByUserUseCase.execute(argThat(r -> r != null && r.page() == 1 && r.size() == 3)))
                .thenReturn(response);
    }

    private void givenEmptyGoalsPage() {
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(), 0, 5, 0),
                page(List.of(), 0, 5, 0));
        when(getUserGoalsByUserUseCase.execute(any(GetUserGoalsRequest.class))).thenReturn(response);
    }

    private void givenUpdateSucceeds() {
        when(updateUserGoalUseCase.execute(argThat(r ->
                r != null && Long.valueOf(1L).equals(r.id()) && "Nuevo".equals(r.title())
                        && "Desc".equals(r.description()) && Long.valueOf(2L).equals(r.activityId()))))
                .thenReturn(new UpdateUserGoalResponse(goalItem(1L, "Nuevo", "PENDING")));
    }

    private void givenCompleteSucceeds(long id) {
        when(completeUserGoalUseCase.execute(argThat(r -> r != null && Long.valueOf(id).equals(r.id())), isNull()))
                .thenReturn(completeResponse(id));
    }

    private UserGoalItem goalItem(Long id, String title, String status) {
        return new UserGoalItem(id, USER_ID, title, "D", status, Instant.now(), 1L, null, 10, 25);
    }

    private UserGoalPage page(List<UserGoalItem> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return new UserGoalPage(content, pageNumber, pageSize, totalElements, totalPages, pageNumber == 0,
                pageNumber >= Math.max(totalPages - 1, 0));
    }

    private CompleteUserGoalResponse completeResponse(Long goalId) {
        UserPlantItem plant = new UserPlantItem(1L, 1, 5, 1L, "GROWING", Instant.now(), null);
        return new CompleteUserGoalResponse(goalItem(goalId, "Meta", "COMPLETED"), false, null, plant);
    }

    // --- act ---
    private ResultActions performAcceptChallenge(String title, String description, Long activityId) throws Exception {
        return mockMvc.perform(post("/api/user-goals/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AcceptChallengeRequest(title, description, activityId))));
    }

    private ResultActions performAdd(String title, String description, Long activityId) throws Exception {
        return mockMvc.perform(post("/api/user-goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserGoalRequest(title, description, activityId))));
    }

    private ResultActions performListByUser() throws Exception {
        return mockMvc.perform(get("/api/user-goals/me"));
    }

    private ResultActions performListByUser(int page, int size) throws Exception {
        return mockMvc.perform(get("/api/user-goals/me")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));
    }

    private ResultActions performUpdate(long id, String title, String description, Long activityId) throws Exception {
        return mockMvc.perform(put("/api/user-goals/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserGoalUpdateRequest(title, description, activityId))));
    }

    private ResultActions performDelete(long id) throws Exception {
        return mockMvc.perform(delete("/api/user-goals/" + id));
    }

    private ResultActions performComplete(long id) throws Exception {
        return mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user-goals/" + id + "/complete"));
    }

    // --- assert ---
    private void thenCreatedWithGoal(ResultActions result, long id, String title, String status) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.status").value(status));
    }

    private void thenCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    private void thenOkWithPaginatedGoals(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.content[0].title").value("Completado"))
                .andExpect(jsonPath("$.completados.totalElements").value(1))
                .andExpect(jsonPath("$.completados.pageSize").value(5))
                .andExpect(jsonPath("$.pendientes.content[0].title").value("Pendiente"))
                .andExpect(jsonPath("$.pendientes.totalElements").value(1));
    }

    private void thenOkWithPageParams(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.pageNumber").value(1))
                .andExpect(jsonPath("$.completados.pageSize").value(3));
    }

    private void thenOkWithEmptyPages(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.content").isEmpty())
                .andExpect(jsonPath("$.pendientes.content").isEmpty());
    }

    private void thenOkWithUpdatedGoal(ResultActions result, long id, String title) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title));
    }

    private void thenOkWithCompletedGoal(ResultActions result, long id) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.goal.id").value(id))
                .andExpect(jsonPath("$.goal.status").value("COMPLETED"));
    }

    private void thenDeleteWasCalledWith(long id) throws Exception {
        verify(deleteUserGoalUseCase).execute(argThat(r -> r != null && Long.valueOf(id).equals(r.id())));
    }

    private void thenCompleteWasCalledWith(long id) throws Exception {
        verify(completeUserGoalUseCase).execute(argThat(r -> r != null && Long.valueOf(id).equals(r.id())), isNull());
    }
}
