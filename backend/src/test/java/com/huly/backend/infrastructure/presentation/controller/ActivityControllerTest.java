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

        ActivityController activityController = new ActivityController(
                listActivitiesUseCase, registerActivitySessionUseCase, new ActivityPresentationMapper());
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
                    "activityType": "BREATHING"
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
                    "activityType": "CHALLENGE"
                }
                """;

        mockMvc.perform(post("/api/activities/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getAllActivities_shouldReturnListOfActivities() throws Exception {
        ListActivitiesResponse response = new ListActivitiesResponse(List.of(
                new ActivityItem(1L, ActivityType.BREATHING,
                        -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 0.3, -0.2, 0.1)
        ));
        when(listActivitiesUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("BREATHING"))
                .andExpect(jsonPath("$[0].effectValence").value(0.3));
    }

    @Test
    void getAllActivities_shouldReturn200WithEmptyListWhenNoActivities() throws Exception {
        when(listActivitiesUseCase.execute()).thenReturn(new ListActivitiesResponse(List.of()));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
