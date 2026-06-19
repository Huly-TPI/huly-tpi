package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.domain.useCase.activities.RegisterActivitySessionUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
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
    private MockMvc mockMvc;
    private ListActivitiesUseCase listActivitiesUseCase;
    private RegisterActivitySessionUseCase registerActivitySessionUseCase;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        listActivitiesUseCase = mock(ListActivitiesUseCase.class);
        registerActivitySessionUseCase = mock(RegisterActivitySessionUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        ActivityController activityController = new ActivityController(listActivitiesUseCase, registerActivitySessionUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(activityController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerSession_shouldReturn200() throws Exception {
        String json = """
                {
                    "activityType": "RESPIRACION"
                }
                """;

        mockMvc.perform(post("/api/activities/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void registerChallengeSession_shouldReturn200() throws Exception {
        String json = """
                {
                    "activityType": "RETO"
                }
                """;

        mockMvc.perform(post("/api/activities/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getAllActivities_shouldReturnListOfActivities() throws Exception {
        List<Activity> activities = List.of(
                Activity.builder()
                        .id(1L)
                        .type(ActivityType.RESPIRACION)
                        .valenceMin(-1.0).valenceMax(1.0)
                        .arousalMin(-1.0).arousalMax(1.0)
                        .dominanceMin(-1.0).dominanceMax(1.0)
                        .effectValence(0.3).effectArousal(-0.2).effectDominance(0.1)
                        .build()
        );
        when(listActivitiesUseCase.execute()).thenReturn(activities);

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("RESPIRACION"))
                .andExpect(jsonPath("$[0].effectValence").value(0.3));
    }

    @Test
    void getAllActivities_shouldReturn200WithEmptyListWhenNoActivities() throws Exception {
        when(listActivitiesUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
