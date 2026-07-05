package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivityCorrelationStats;
import com.huly.backend.domain.model.activity.ActivityImpactStats;
import com.huly.backend.domain.model.activity.ActivityPopularityStats;
import com.huly.backend.domain.model.activity.ActivitiesKpiStats;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.useCase.admin.activities.GetActivitiesKpiUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityCorrelationUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityImpactUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetActivityPopularityUseCase;
import com.huly.backend.domain.useCase.admin.activities.GetAdminActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.activities.UpdateActivityConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.activities.AdminUpdateActivityConfigRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminActivityControllerTest {

    private MockMvc mockMvc;
    private GetAdminActivitiesUseCase getAdminActivitiesUseCase;
    private UpdateActivityConfigUseCase updateActivityConfigUseCase;
    private GetActivitiesKpiUseCase getActivitiesKpiUseCase;
    private GetActivityPopularityUseCase getActivityPopularityUseCase;
    private GetActivityCorrelationUseCase getActivityCorrelationUseCase;
    private GetActivityImpactUseCase getActivityImpactUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        getAdminActivitiesUseCase = mock(GetAdminActivitiesUseCase.class);
        updateActivityConfigUseCase = mock(UpdateActivityConfigUseCase.class);
        getActivitiesKpiUseCase = mock(GetActivitiesKpiUseCase.class);
        getActivityPopularityUseCase = mock(GetActivityPopularityUseCase.class);
        getActivityCorrelationUseCase = mock(GetActivityCorrelationUseCase.class);
        getActivityImpactUseCase = mock(GetActivityImpactUseCase.class);

        AdminActivityController controller = new AdminActivityController(
                getAdminActivitiesUseCase,
                updateActivityConfigUseCase,
                getActivitiesKpiUseCase,
                getActivityPopularityUseCase,
                getActivityCorrelationUseCase,
                getActivityImpactUseCase,
                new AdminPresentationMapper()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAdminActivities_shouldReturnList() throws Exception {
        Activity act = Activity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .valenceMin(-0.1).valenceMax(0.2)
                .arousalMin(-0.3).arousalMax(0.4)
                .dominanceMin(-0.5).dominanceMax(0.6)
                .effectValence(0.05).effectArousal(0.1).effectDominance(-0.1)
                .title("Respiracion").description("Desc")
                .goalKeywords("calma").routePath("/breath")
                .build();

        when(getAdminActivitiesUseCase.execute()).thenReturn(List.of(act));

        mockMvc.perform(get("/api/admin/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("BREATHING"))
                .andExpect(jsonPath("$[0].valenceMin").value(-0.1))
                .andExpect(jsonPath("$[0].valenceMax").value(0.2))
                .andExpect(jsonPath("$[0].arousalMin").value(-0.3))
                .andExpect(jsonPath("$[0].arousalMax").value(0.4))
                .andExpect(jsonPath("$[0].dominanceMin").value(-0.5))
                .andExpect(jsonPath("$[0].dominanceMax").value(0.6))
                .andExpect(jsonPath("$[0].effectValence").value(0.05))
                .andExpect(jsonPath("$[0].effectArousal").value(0.1))
                .andExpect(jsonPath("$[0].effectDominance").value(-0.1))
                .andExpect(jsonPath("$[0].title").value("Respiracion"))
                .andExpect(jsonPath("$[0].description").value("Desc"))
                .andExpect(jsonPath("$[0].goalKeywords").value("calma"))
                .andExpect(jsonPath("$[0].routePath").value("/breath"));
    }

    @Test
    void updateActivityConfig_shouldReturnUpdated() throws Exception {
        AdminUpdateActivityConfigRequest request = new AdminUpdateActivityConfigRequest();
        request.setValenceMin(-0.2);
        request.setValenceMax(0.3);
        request.setArousalMin(-0.4);
        request.setArousalMax(0.5);
        request.setDominanceMin(-0.6);
        request.setDominanceMax(0.7);
        request.setEffectValence(0.1);
        request.setEffectArousal(-0.1);
        request.setEffectDominance(0.2);
        request.setTitle("Nuevo Titulo");
        request.setDescription("Nueva Desc");
        request.setGoalKeywords("paz");
        request.setRoutePath("/new");

        Activity act = Activity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .valenceMin(-0.2).valenceMax(0.3)
                .arousalMin(-0.4).arousalMax(0.5)
                .dominanceMin(-0.6).dominanceMax(0.7)
                .effectValence(0.1).effectArousal(-0.1).effectDominance(0.2)
                .title("Nuevo Titulo").description("Nueva Desc")
                .goalKeywords("paz").routePath("/new")
                .build();

        when(updateActivityConfigUseCase.execute(eq(1L), any())).thenReturn(act);

        mockMvc.perform(put("/api/admin/activities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nuevo Titulo"));
    }

    @Test
    void getDashboardKpi_shouldReturnKpi() throws Exception {
        ActivitiesKpiStats stats = ActivitiesKpiStats.builder()
                .totalSessions(100L)
                .topActivityType("BUBBLE")
                .topActivitySessions(45L)
                .averageMoodImprovement(0.45)
                .build();
        when(getActivitiesKpiUseCase.execute(Timeframe.WEEK)).thenReturn(stats);

        mockMvc.perform(get("/api/admin/activities/kpis").param("timeframe", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(100))
                .andExpect(jsonPath("$.topActivity.type").value("BUBBLE"))
                .andExpect(jsonPath("$.topActivity.sessions").value(45))
                .andExpect(jsonPath("$.averageMoodImprovement").value(0.45));
    }

    @Test
    void getActivityPopularity_shouldReturnPopularity() throws Exception {
        ActivityPopularityStats stats = ActivityPopularityStats.builder()
                .activityType("LANTERN")
                .activityName("Farolitos")
                .totalSessions(23L)
                .build();
        when(getActivityPopularityUseCase.execute(Timeframe.MONTH)).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/admin/activities/popularity").param("timeframe", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityType").value("LANTERN"))
                .andExpect(jsonPath("$[0].activityName").value("Farolitos"))
                .andExpect(jsonPath("$[0].totalSessions").value(23));
    }

    @Test
    void getActivityCorrelation_shouldReturnCorrelation() throws Exception {
        ActivityCorrelationStats stats = ActivityCorrelationStats.builder()
                .activityType("DIARY")
                .emotion("Tristeza")
                .suggestionsCount(15L)
                .acceptanceRate(0.85)
                .build();
        when(getActivityCorrelationUseCase.execute(Timeframe.TOTAL)).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/admin/activities/correlation").param("timeframe", "total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityType").value("DIARY"))
                .andExpect(jsonPath("$[0].emotion").value("Tristeza"))
                .andExpect(jsonPath("$[0].suggestionsCount").value(15))
                .andExpect(jsonPath("$[0].acceptanceRate").value(0.85));
    }

    @Test
    void getActivityImpact_shouldReturnImpact() throws Exception {
        ActivityImpactStats stats = ActivityImpactStats.builder()
                .activityType("MANDALA")
                .averageValenceChange(0.35)
                .averageArousalChange(-0.15)
                .basedOnMetrics(true)
                .build();
        when(getActivityImpactUseCase.execute(Timeframe.TODAY)).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/admin/activities/impact").param("timeframe", "today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityType").value("MANDALA"))
                .andExpect(jsonPath("$[0].averageValenceChange").value(0.35))
                .andExpect(jsonPath("$[0].averageArousalChange").value(-0.15))
                .andExpect(jsonPath("$[0].basedOnMetrics").value(true));
    }
}
