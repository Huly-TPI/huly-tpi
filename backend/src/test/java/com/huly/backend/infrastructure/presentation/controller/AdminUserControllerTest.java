package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigResponse;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigRequest;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesRequest;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsResponse;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsResponse;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsResponse;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private MockMvc mockMvc;
    private ListBackofficeUsersUseCase listBackofficeUsersUseCase;
    private GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private GetAntiScrollGlobalConfigUseCase getAntiScrollGlobalConfigUseCase;
    private UpdateAntiScrollGlobalConfigUseCase updateAntiScrollGlobalConfigUseCase;
    private GetUserActivitiesUseCase getUserActivitiesUseCase;
    private GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase;
    private GetUserFinancialsUseCase getUserFinancialsUseCase;
    private GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listBackofficeUsersUseCase = mock(ListBackofficeUsersUseCase.class);
        getAntiScrollDashboardUseCase = mock(GetAntiScrollDashboardUseCase.class);
        getAntiScrollGlobalConfigUseCase = mock(GetAntiScrollGlobalConfigUseCase.class);
        updateAntiScrollGlobalConfigUseCase = mock(UpdateAntiScrollGlobalConfigUseCase.class);
        getUserActivitiesUseCase = mock(GetUserActivitiesUseCase.class);
        getUserAiDiagnosticsUseCase = mock(GetUserAiDiagnosticsUseCase.class);
        getUserFinancialsUseCase = mock(GetUserFinancialsUseCase.class);
        getUserAntiScrollStatsUseCase = mock(GetUserAntiScrollStatsUseCase.class);

        when(getAntiScrollGlobalConfigUseCase.execute()).thenReturn(new GetAntiScrollGlobalConfigResponse(
                20,
                "El modo anti-scroll es simplemente una herramienta para acompa\u00f1arte cuando sientas que necesit\u00e1s frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentraci\u00f3n o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. \u00a1Cero presiones, el ritmo lo marc\u00e1s vos!"
        ));

        AdminUserController controller = new AdminUserController(
                listBackofficeUsersUseCase,
                getAntiScrollDashboardUseCase,
                getAntiScrollGlobalConfigUseCase,
                updateAntiScrollGlobalConfigUseCase,
                getUserActivitiesUseCase,
                getUserAiDiagnosticsUseCase,
                getUserFinancialsUseCase,
                getUserAntiScrollStatsUseCase,
                new AdminPresentationMapper()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getBackofficeUsers_shouldReturnList() throws Exception {
        BackofficeUserSummary summary = BackofficeUserSummary.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .antiScrollEnabled(true)
                .dataSharingConsent(true)
                .mostUsedApp("instagram.com")
                .mostUsedAppActiveSeconds(3600)
                .totalScrollTimeSeconds(5000)
                .dailyScrollTimeSeconds(java.util.Map.of("current_0", 100))
                .topApps(List.of(new TopAppStats("instagram.com", 3600)))
                .coins(100)
                .plan("PREMIUM_PLAN")
                .build();

        when(listBackofficeUsersUseCase.execute(any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].antiScrollEnabled").value(true))
                .andExpect(jsonPath("$[0].dataSharingConsent").value(true))
                .andExpect(jsonPath("$[0].mostUsedApp").value("instagram.com"))
                .andExpect(jsonPath("$[0].mostUsedAppActiveSeconds").value(3600))
                .andExpect(jsonPath("$[0].totalScrollTimeSeconds").value(5000))
                .andExpect(jsonPath("$[0].dailyScrollTimeSeconds.current_0").value(100))
                .andExpect(jsonPath("$[0].topApps[0].domain").value("instagram.com"))
                .andExpect(jsonPath("$[0].topApps[0].totalActiveSeconds").value(3600))
                .andExpect(jsonPath("$[0].coins").value(100))
                .andExpect(jsonPath("$[0].plan").value("PREMIUM_PLAN"));
    }

    @Test
    void getBackofficeUsers_withSearchParam_shouldPassSearchParamToUseCase() throws Exception {
        when(listBackofficeUsersUseCase.execute("alice")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").param("search", "alice"))
                .andExpect(status().isOk());

        verify(listBackofficeUsersUseCase).execute("alice");
    }

    @Test
    void getDashboardStats_shouldReturnStats() throws Exception {
        AntiScrollDashboardStats stats = AntiScrollDashboardStats.builder()
                .totalModalsShown(100)
                .totalRedirects(40)
                .totalUsersCount(50)
                .activeExtensionUsersCount(30)
                .dataSharingConsentUsersCount(20)
                .topUsedApps(List.of(new TopAppStats("instagram.com", 3600)))
                .build();

        when(getAntiScrollDashboardUseCase.execute()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/users/antiscroll/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalModalsShown").value(100))
                .andExpect(jsonPath("$.totalRedirects").value(40))
                .andExpect(jsonPath("$.totalUsersCount").value(50))
                .andExpect(jsonPath("$.activeExtensionUsersCount").value(30))
                .andExpect(jsonPath("$.dataSharingConsentUsersCount").value(20))
                .andExpect(jsonPath("$.topUsedApps[0].domain").value("instagram.com"))
                .andExpect(jsonPath("$.topUsedApps[0].totalActiveSeconds").value(3600));
    }

    @Test
    void getAntiScrollGlobalConfig_shouldReturnConfig() throws Exception {
        when(getAntiScrollGlobalConfigUseCase.execute()).thenReturn(new GetAntiScrollGlobalConfigResponse(25, "terminos de prueba"));

        mockMvc.perform(get("/api/admin/users/antiscroll/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultPauseIntervalMinutes").value(25))
                .andExpect(jsonPath("$.termsAndConditions").value("terminos de prueba"));
    }

    @Test
    void getBackofficeUsers_shouldHandleNulls() throws Exception {
        BackofficeUserSummary summary = BackofficeUserSummary.builder()
                .id(3L)
                .name("Jane Doe")
                .email("jane@example.com")
                .role(null)
                .status(null)
                .birthDate(LocalDate.of(2000, 1, 1))
                .antiScrollEnabled(false)
                .dataSharingConsent(false)
                .mostUsedApp(null)
                .mostUsedAppActiveSeconds(0)
                .totalScrollTimeSeconds(0)
                .dailyScrollTimeSeconds(null)
                .topApps(null)
                .coins(null)
                .plan(null)
                .build();

        when(listBackofficeUsersUseCase.execute(any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].email").value("jane@example.com"))
                .andExpect(jsonPath("$[0].role").isEmpty())
                .andExpect(jsonPath("$[0].status").isEmpty())
                .andExpect(jsonPath("$[0].antiScrollEnabled").value(false))
                .andExpect(jsonPath("$[0].dataSharingConsent").value(false))
                .andExpect(jsonPath("$[0].mostUsedApp").isEmpty())
                .andExpect(jsonPath("$[0].topApps").isEmpty());
    }

    @Test
    void getAntiScrollGlobalConfig_shouldReturnDefaultConfig() throws Exception {
        mockMvc.perform(get("/api/admin/users/antiscroll/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultPauseIntervalMinutes").value(20))
                .andExpect(jsonPath("$.termsAndConditions").value("El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!"));
    }

    @Test
    void updateAntiScrollGlobalConfig_shouldSaveConfig() throws Exception {
        com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest request = new com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest(15, "nuevos terminos");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/antiscroll/config")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(updateAntiScrollGlobalConfigUseCase).execute(new UpdateAntiScrollGlobalConfigRequest(15, "nuevos terminos"));
    }

    @Test
    void updateAntiScrollGlobalConfig_shouldDelegateRequestAsIs() throws Exception {
        com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest request = new com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest(15, "nuevos terminos");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/antiscroll/config")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(updateAntiScrollGlobalConfigUseCase).execute(new UpdateAntiScrollGlobalConfigRequest(15, "nuevos terminos"));
    }

    @Test
    void getUserActivities_shouldReturnActivities() throws Exception {
        com.huly.backend.domain.useCase.admin.userActivities.ActivitySessionResponse session =
                new com.huly.backend.domain.useCase.admin.userActivities.ActivitySessionResponse(1L, "BREATHING", java.time.Instant.now());
        GetUserActivitiesResponse result = new GetUserActivitiesResponse(java.util.List.of(session), 5L, "BREATHING", "1.5", java.util.Map.of("BREATHING", 5));
        when(getUserActivitiesUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayActivitiesCount").value(5))
                .andExpect(jsonPath("$.activitySessions[0].id").value(1))
                .andExpect(jsonPath("$.activitySessions[0].activityType").value("BREATHING"))
                .andExpect(jsonPath("$.favoriteActivity").value("BREATHING"))
                .andExpect(jsonPath("$.averageSessionsText").value("1.5"))
                .andExpect(jsonPath("$.activityDistribution.BREATHING").value(5));
    }

    @Test
    void getUserActivities_withSpecificTimeframe_shouldPassConvertedTimeframeToUseCase() throws Exception {
        GetUserActivitiesResponse result = new GetUserActivitiesResponse(java.util.List.of(), 5L, null, null, null);
        when(getUserActivitiesUseCase.execute(eq(new GetUserActivitiesRequest(2L, Timeframe.WEEK)))).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/activities").param("timeframe", "week"))
                .andExpect(status().isOk());

        verify(getUserActivitiesUseCase).execute(new GetUserActivitiesRequest(2L, Timeframe.WEEK));
    }

    @Test
    void getUserActivities_withInvalidTimeframe_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/api/admin/users/2/statistics/activities").param("timeframe", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserAiDiagnostics_shouldReturnAiDiagnostics() throws Exception {
        com.huly.backend.domain.useCase.admin.userAiDiagnostics.VectorMemoryResponse memory =
                new com.huly.backend.domain.useCase.admin.userAiDiagnostics.VectorMemoryResponse("mem-1", "content", "USER", "FACT", "2026-06-16T00:00:00Z");
        com.huly.backend.domain.useCase.admin.userAiDiagnostics.EmotionalEventResponse event =
                new com.huly.backend.domain.useCase.admin.userAiDiagnostics.EmotionalEventResponse(
                        10L, "source", "inputText", "detectedEmotion", 0.9, 0.8, 0.7, 0.6, 0.5, "userGoal", "recommendation", 100L, 200L, "ACCEPTED", 5, "feedbackText", java.time.Instant.now()
                );
        GetUserAiDiagnosticsResponse result = new GetUserAiDiagnosticsResponse(
                java.util.List.of(memory), java.util.List.of(event), "Alice", "style", "summary",
                java.util.List.of("topic1"), java.util.List.of("strategy1"), 80, "HIGH", java.util.List.of("act1"), java.util.List.of("act2"), "JOY", java.util.Map.of("JOY", 10)
        );
        when(getUserAiDiagnosticsUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiMemories[0].id").value("mem-1"))
                .andExpect(jsonPath("$.aiMemories[0].content").value("content"))
                .andExpect(jsonPath("$.emotionalEvents[0].id").value(10))
                .andExpect(jsonPath("$.emotionalEvents[0].inputText").value("inputText"))
                .andExpect(jsonPath("$.preferredName").value("Alice"))
                .andExpect(jsonPath("$.receptivityScore").value(80))
                .andExpect(jsonPath("$.receptivityLabel").value("HIGH"))
                .andExpect(jsonPath("$.dominantEmotion").value("JOY"));
    }

    @Test
    void getUserFinancials_shouldReturnFinancials() throws Exception {
        com.huly.backend.domain.useCase.admin.userFinancials.PaymentEventResponse payment =
                new com.huly.backend.domain.useCase.admin.userFinancials.PaymentEventResponse(
                        100L, 200L, "product", java.math.BigDecimal.TEN, "ref", 300L, "SUCCESS", 100, "COIN_PACK", java.time.Instant.now()
                );
        GetUserFinancialsResponse result = new GetUserFinancialsResponse(java.util.List.of(payment), java.math.BigDecimal.TEN);
        when(getUserFinancialsUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentEvents[0].id").value(100))
                .andExpect(jsonPath("$.paymentEvents[0].productName").value("product"))
                .andExpect(jsonPath("$.totalEarnings").value(10));
    }

    @Test
    void getUserAntiScrollStats_shouldReturnStats() throws Exception {
        GetUserAntiScrollStatsResponse result = new GetUserAntiScrollStatsResponse(
                true,
                true,
                "instagram.com",
                300,
                500,
                java.util.Map.of("current_0", 100),
                List.of(new TopAppStats("instagram.com", 300))
        );
        when(getUserAntiScrollStatsUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/antiscroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antiScrollEnabled").value(true))
                .andExpect(jsonPath("$.dataSharingConsent").value(true))
                .andExpect(jsonPath("$.mostUsedApp").value("instagram.com"))
                .andExpect(jsonPath("$.mostUsedAppActiveSeconds").value(300))
                .andExpect(jsonPath("$.totalScrollTimeSeconds").value(500))
                .andExpect(jsonPath("$.dailyScrollTimeSeconds.current_0").value(100))
                .andExpect(jsonPath("$.topApps[0].domain").value("instagram.com"))
                .andExpect(jsonPath("$.topApps[0].totalActiveSeconds").value(300));
    }
}
