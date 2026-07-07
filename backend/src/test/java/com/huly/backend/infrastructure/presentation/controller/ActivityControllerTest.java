package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.activities.ActivityItem;
import com.huly.backend.domain.dto.activities.ListActivitiesResponse;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.domain.useCase.activities.RegisterActivitySessionUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.activities.ActivityPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivityControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private ListActivitiesUseCase listActivitiesUseCase;
    private RegisterActivitySessionUseCase registerActivitySessionUseCase;

    @BeforeEach
    void setUp() {
        listActivitiesUseCase = mock(ListActivitiesUseCase.class);
        registerActivitySessionUseCase = mock(RegisterActivitySessionUseCase.class);

        ActivityController activityController = new ActivityController(
                listActivitiesUseCase, registerActivitySessionUseCase, new ActivityPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(activityController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 al registrar una sesión de respiración")
    void registerSessionShouldReturn200() throws Exception {
        ResultActions result = performRegisterSession("BREATHING");

        thenOk(result);
    }

    @Test
    @DisplayName("Devuelve 200 al registrar una sesión de reto")
    void registerChallengeSessionShouldReturn200() throws Exception {
        ResultActions result = performRegisterSession("CHALLENGE");

        thenOk(result);
    }

    @Test
    @DisplayName("Devuelve 401 al registrar una sesión sin estar autenticado")
    void registerSessionShouldReturn401WhenNotAuthenticated() throws Exception {
        givenNoAuthentication();

        ResultActions result = performRegisterSession("BREATHING");

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 401 al registrar una sesión cuando el usuario del principal no es numérico")
    void registerSessionShouldReturn401WhenPrincipalUsernameNotNumeric() throws Exception {
        authenticateAs("not-a-number");

        ResultActions result = performRegisterSession("BREATHING");

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve la lista de actividades")
    void getAllActivitiesShouldReturnListOfActivities() throws Exception {
        givenActivitiesAvailable();

        ResultActions result = performGetActivities();

        thenOkWithActivityList(result);
    }

    @Test
    @DisplayName("Devuelve 200 con lista vacía cuando no hay actividades")
    void getAllActivitiesShouldReturn200WithEmptyListWhenNoActivities() throws Exception {
        givenNoActivities();

        ResultActions result = performGetActivities();

        thenOkWithEmptyArray(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenActivitiesAvailable() {
        ListActivitiesResponse response = new ListActivitiesResponse(List.of(
                new ActivityItem(1L, ActivityType.BREATHING,
                        -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.3, -0.2, 0.1)
        ));
        when(listActivitiesUseCase.execute()).thenReturn(response);
    }

    private void givenNoActivities() {
        when(listActivitiesUseCase.execute()).thenReturn(new ListActivitiesResponse(List.of()));
    }

    // --- act ---
    private ResultActions performRegisterSession(String activityType) throws Exception {
        String json = """
                {
                    "activityType": "%s"
                }
                """.formatted(activityType);
        return mockMvc.perform(post("/api/activities/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performGetActivities() throws Exception {
        return mockMvc.perform(get("/api/activities"));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenOkWithActivityList(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("BREATHING"))
                .andExpect(jsonPath("$[0].effectValence").value(0.3));
    }

    private void thenOkWithEmptyArray(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
