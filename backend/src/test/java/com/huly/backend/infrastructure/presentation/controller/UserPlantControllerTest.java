package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.UserPlant;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.repository.UserPlantRepository;
import com.huly.backend.domain.useCase.userPlant.GetOrCreateCurrentPlantUseCase;
import com.huly.backend.domain.useCase.userPlant.GetPlantGoalsUseCase;
import com.huly.backend.domain.useCase.userPlant.GetUserPlantsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserPlantControllerTest {

    private MockMvc mockMvc;
    private GetOrCreateCurrentPlantUseCase getOrCreateCurrentPlantUseCase;
    private GetUserPlantsUseCase getUserPlantsUseCase;
    private GetPlantGoalsUseCase getPlantGoalsUseCase;
    private UserPlantRepository userPlantRepository;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getOrCreateCurrentPlantUseCase = mock(GetOrCreateCurrentPlantUseCase.class);
        getUserPlantsUseCase = mock(GetUserPlantsUseCase.class);
        getPlantGoalsUseCase = mock(GetPlantGoalsUseCase.class);
        userPlantRepository = mock(UserPlantRepository.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        UserPlantController controller = new UserPlantController(
                getOrCreateCurrentPlantUseCase,
                getUserPlantsUseCase,
                getPlantGoalsUseCase,
                userPlantRepository
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
        UserPlant plant = UserPlant.builder()
                .id(10L)
                .userId(USER_ID)
                .plantNumber(3)
                .requiredGoals(5)
                .status(PlantStatus.GROWING)
                .startedAt(Instant.parse("2026-06-01T12:00:00Z"))
                .completedGoalsCount(2L)
                .build();
        when(getOrCreateCurrentPlantUseCase.execute(USER_ID)).thenReturn(plant);

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
        UserPlant plant = UserPlant.builder()
                .id(10L)
                .userId(USER_ID)
                .plantNumber(1)
                .requiredGoals(5)
                .status(PlantStatus.COMPLETED)
                .startedAt(Instant.parse("2026-06-01T12:00:00Z"))
                .completedAt(Instant.parse("2026-06-05T12:00:00Z"))
                .build();
        when(getUserPlantsUseCase.execute(USER_ID)).thenReturn(List.of(plant));
        when(userPlantRepository.countCompletedGoalsByPlantId(10L)).thenReturn(5L);

        mockMvc.perform(get("/api/user-plants/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].completedGoalsCount").value(5))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getGoals_shouldReturnPlantGoals() throws Exception {
        UserGoal goal = UserGoal.builder()
                .id(100L)
                .userId(USER_ID)
                .title("Meta de prueba")
                .description("Descripción")
                .status(GoalStatus.COMPLETED)
                .createdAt(Instant.parse("2026-06-01T12:00:00Z"))
                .coinsReward(10)
                .coinsRewardWithImage(20)
                .build();
        when(getPlantGoalsUseCase.execute(10L)).thenReturn(List.of(goal));

        mockMvc.perform(get("/api/user-plants/10/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plantId").value(10))
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals[0].id").value(100))
                .andExpect(jsonPath("$.goals[0].title").value("Meta de prueba"))
                .andExpect(jsonPath("$.goals[0].status").value("COMPLETED"));
    }
}
