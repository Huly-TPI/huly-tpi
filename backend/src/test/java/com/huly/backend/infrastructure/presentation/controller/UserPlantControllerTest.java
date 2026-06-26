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
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserPlantControllerTest {

    private MockMvc mockMvc;
    private GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private GetUserPlantsUseCase getUserPlantsUseCase;
    private GetPlantGoalsUseCase getPlantGoalsUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getOrCreateCurrentPlantUseCase = mock(GetOrCreateCurrentPlantUseCase.class);
        getUserPlantsUseCase = mock(GetUserPlantsUseCase.class);
        getPlantGoalsUseCase = mock(GetPlantGoalsUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrent_shouldReturnCurrentPlant() throws Exception {
        UserPlantItem plant = new UserPlantItem(
                10L, 3, 5, 2L, "GROWING", Instant.parse("2026-06-01T12:00:00Z"), null);
        when(getOrCreateCurrentPlantUseCase.execute(argThat((GetCurrentPlantRequest r) ->
                r != null && USER_ID.equals(r.userId()))))
                .thenReturn(new GetCurrentPlantResponse(plant));

        mockMvc.perform(get("/api/user-plants/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.plantNumber").value(3))
                .andExpect(jsonPath("$.requiredGoals").value(5))
                .andExpect(jsonPath("$.completedGoalsCount").value(2))
                .andExpect(jsonPath("$.status").value("GROWING"));
    }

    @Test
    void getAll_shouldReturnAllUserPlants() throws Exception {
        UserPlantItem plant = new UserPlantItem(
                10L, 1, 5, 5L, "COMPLETED",
                Instant.parse("2026-06-01T12:00:00Z"), Instant.parse("2026-06-05T12:00:00Z"));
        when(getUserPlantsUseCase.execute(argThat((GetUserPlantsRequest r) ->
                r != null && USER_ID.equals(r.userId()))))
                .thenReturn(new GetUserPlantsResponse(List.of(plant)));

        mockMvc.perform(get("/api/user-plants/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].completedGoalsCount").value(5))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getGoals_shouldReturnPlantGoals() throws Exception {
        UserGoalItem goal = new UserGoalItem(
                100L, USER_ID, "Meta de prueba", "Descripción", "COMPLETED",
                Instant.parse("2026-06-01T12:00:00Z"), null, null, 10, 20);
        when(getPlantGoalsUseCase.execute(argThat((GetPlantGoalsRequest r) ->
                r != null && Long.valueOf(10L).equals(r.plantId()))))
                .thenReturn(new GetPlantGoalsResponse(10L, List.of(goal)));

        mockMvc.perform(get("/api/user-plants/10/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plantId").value(10))
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals[0].id").value(100))
                .andExpect(jsonPath("$.goals[0].title").value("Meta de prueba"))
                .andExpect(jsonPath("$.goals[0].status").value("COMPLETED"));
    }
}
