package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantRequest;
import com.huly.backend.domain.dto.userPlant.GetCurrentPlantResponse;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsRequest;
import com.huly.backend.domain.dto.userPlant.GetPlantGoalsResponse;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsRequest;
import com.huly.backend.domain.dto.userPlant.GetUserPlantsResponse;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.useCase.userPlant.GetPlantGoalsUseCase;
import com.huly.backend.domain.useCase.userPlant.GetUserPlantsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.userPlant.UserPlantPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserPlantControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private GetUserPlantsUseCase getUserPlantsUseCase;
    private GetPlantGoalsUseCase getPlantGoalsUseCase;

    @BeforeEach
    void setUp() {
        getOrCreateCurrentPlantUseCase = mock(GetOrCreateCurrentPlantUseCase.class);
        getUserPlantsUseCase = mock(GetUserPlantsUseCase.class);
        getPlantGoalsUseCase = mock(GetPlantGoalsUseCase.class);

        UserPlantController controller = new UserPlantController(
                getOrCreateCurrentPlantUseCase,
                getUserPlantsUseCase,
                getPlantGoalsUseCase,
                new UserPlantPresentationMapper()
        );

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

    @Test
    @DisplayName("Devuelve 200 con la planta actual del usuario")
    void getCurrentShouldReturnCurrentPlant() throws Exception {
        givenCurrentPlant();

        ResultActions result = performGetCurrent();

        thenOkWithCurrentPlant(result);
    }

    @Test
    @DisplayName("Devuelve 401 al pedir la planta actual sin estar autenticado")
    void getCurrentShouldReturn401WhenNotAuthenticated() throws Exception {
        givenNoAuthentication();

        ResultActions result = performGetCurrent();

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 200 con todas las plantas del usuario")
    void getAllShouldReturnAllUserPlants() throws Exception {
        givenUserPlants();

        ResultActions result = performGetAll();

        thenOkWithAllPlants(result);
    }

    @Test
    @DisplayName("Devuelve 200 con las metas de una planta")
    void getGoalsShouldReturnPlantGoals() throws Exception {
        givenPlantGoals();

        ResultActions result = performGetGoals(10L);

        thenOkWithPlantGoals(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenCurrentPlant() {
        UserPlantItem plant = new UserPlantItem(
                10L, 3, 5, 2L, "GROWING", Instant.parse("2026-06-01T12:00:00Z"), null);
        when(getOrCreateCurrentPlantUseCase.execute(argThat((GetCurrentPlantRequest r) ->
                r != null && USER_ID.equals(r.userId()))))
                .thenReturn(new GetCurrentPlantResponse(plant));
    }

    private void givenUserPlants() {
        UserPlantItem plant = new UserPlantItem(
                10L, 1, 5, 5L, "COMPLETED",
                Instant.parse("2026-06-01T12:00:00Z"), Instant.parse("2026-06-05T12:00:00Z"));
        when(getUserPlantsUseCase.execute(argThat((GetUserPlantsRequest r) ->
                r != null && USER_ID.equals(r.userId()))))
                .thenReturn(new GetUserPlantsResponse(List.of(plant)));
    }

    private void givenPlantGoals() {
        UserGoalItem goal = new UserGoalItem(
                100L, USER_ID, "Meta de prueba", "Descripción", "COMPLETED",
                Instant.parse("2026-06-01T12:00:00Z"), null, null, 10, 20);
        when(getPlantGoalsUseCase.execute(argThat((GetPlantGoalsRequest r) ->
                r != null && Long.valueOf(10L).equals(r.plantId()))))
                .thenReturn(new GetPlantGoalsResponse(10L, List.of(goal)));
    }

    // --- act ---
    private ResultActions performGetCurrent() throws Exception {
        return mockMvc.perform(get("/api/user-plants/current"));
    }

    private ResultActions performGetAll() throws Exception {
        return mockMvc.perform(get("/api/user-plants/me"));
    }

    private ResultActions performGetGoals(long id) throws Exception {
        return mockMvc.perform(get("/api/user-plants/" + id + "/goals"));
    }

    // --- assert ---
    private void thenOkWithCurrentPlant(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.plantNumber").value(3))
                .andExpect(jsonPath("$.requiredGoals").value(5))
                .andExpect(jsonPath("$.completedGoalsCount").value(2))
                .andExpect(jsonPath("$.status").value("GROWING"));
    }

    private void thenOkWithAllPlants(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].completedGoalsCount").value(5))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    private void thenOkWithPlantGoals(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.plantId").value(10))
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals[0].id").value(100))
                .andExpect(jsonPath("$.goals[0].title").value("Meta de prueba"))
                .andExpect(jsonPath("$.goals[0].status").value("COMPLETED"));
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
