package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsResponse;
import com.huly.backend.domain.useCase.admin.userAiDiagnostics.GetUserAiDiagnosticsUseCase;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsResponse;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsResponse;
import com.huly.backend.infrastructure.presentation.controller.AdminUserController;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
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
    private com.huly.backend.domain.repository.extension.AntiScrollConfigRepository antiScrollConfigRepository;
    private GetUserActivitiesUseCase getUserActivitiesUseCase;
    private GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase;
    private GetUserFinancialsUseCase getUserFinancialsUseCase;
    private GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listBackofficeUsersUseCase = mock(ListBackofficeUsersUseCase.class);
        getAntiScrollDashboardUseCase = mock(GetAntiScrollDashboardUseCase.class);
        antiScrollConfigRepository = mock(com.huly.backend.domain.repository.extension.AntiScrollConfigRepository.class);
        getUserActivitiesUseCase = mock(GetUserActivitiesUseCase.class);
        getUserAiDiagnosticsUseCase = mock(GetUserAiDiagnosticsUseCase.class);
        getUserFinancialsUseCase = mock(GetUserFinancialsUseCase.class);
        getUserAntiScrollStatsUseCase = mock(GetUserAntiScrollStatsUseCase.class);

        when(antiScrollConfigRepository.findFirst()).thenReturn(java.util.Optional.empty());

        AdminUserController controller = new AdminUserController(
                listBackofficeUsersUseCase,
                getAntiScrollDashboardUseCase,
                antiScrollConfigRepository,
                getUserActivitiesUseCase,
                getUserAiDiagnosticsUseCase,
                getUserFinancialsUseCase,
                getUserAntiScrollStatsUseCase
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
    void getAntiScrollConfig_shouldReturnConfig() throws Exception {
        com.huly.backend.domain.model.extension.AntiScrollConfig config = com.huly.backend.domain.model.extension.AntiScrollConfig.builder()
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("terminos de prueba")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(java.util.Optional.of(config));

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
    void getAntiScrollConfig_shouldReturnDefaultConfig() throws Exception {
        when(antiScrollConfigRepository.findFirst()).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/admin/users/antiscroll/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultPauseIntervalMinutes").value(20))
                .andExpect(jsonPath("$.termsAndConditions").value("El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!"));
    }

    @Test
    void updateAntiScrollConfig_shouldSaveConfig() throws Exception {
        com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest request = new com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest(15, "nuevos terminos");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/antiscroll/config")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(antiScrollConfigRepository).save(any());
    }

    @Test
    void updateAntiScrollConfig_shouldSaveConfig_whenAlreadyExists() throws Exception {
        com.huly.backend.domain.model.extension.AntiScrollConfig config = com.huly.backend.domain.model.extension.AntiScrollConfig.builder()
                .id(1L)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("terminos existentes")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(java.util.Optional.of(config));

        com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest request = new com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest(15, "nuevos terminos");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/antiscroll/config")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(antiScrollConfigRepository).save(argThat(c -> c.getId() == 1L && c.getDefaultPauseIntervalMinutes() == 15));
    }

    @Test
    void getUserActivities_shouldReturnActivities() throws Exception {
        GetUserActivitiesResponse result = new GetUserActivitiesResponse(java.util.List.of(), 5L, null, null, null);
        when(getUserActivitiesUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayActivitiesCount").value(5));
    }

    @Test
    void getUserAiDiagnostics_shouldReturnAiDiagnostics() throws Exception {
        GetUserAiDiagnosticsResponse result = new GetUserAiDiagnosticsResponse(java.util.List.of(), java.util.List.of(), null, null, null, null, null, 0, null, null, null, null, null);
        when(getUserAiDiagnosticsUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiMemories").isArray());
    }

    @Test
    void getUserFinancials_shouldReturnFinancials() throws Exception {
        GetUserFinancialsResponse result = new GetUserFinancialsResponse(java.util.List.of(), java.math.BigDecimal.TEN);
        when(getUserFinancialsUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/admin/users/2/statistics/finance"))
                .andExpect(status().isOk())
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
