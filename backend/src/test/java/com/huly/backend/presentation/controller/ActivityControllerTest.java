package com.huly.backend.presentation.controller;
import com.huly.backend.domain.model.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.useCase.activities.ListActivitiesUseCase;
import com.huly.backend.infrastructure.presentation.controller.ActivityController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ActivityControllerTest {
    private MockMvc mockMvc;
    private ListActivitiesUseCase listActivitiesUseCase;

    @BeforeEach
    void setUp() {
        listActivitiesUseCase = mock(ListActivitiesUseCase.class);
        ActivityController activityController = new ActivityController(listActivitiesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build();
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