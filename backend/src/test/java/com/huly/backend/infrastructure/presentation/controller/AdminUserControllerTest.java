package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
import com.huly.backend.domain.useCase.admin.userActivities.ActivitySessionResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserControllerTest {

    private MockMvc mockMvc;
    private ListBackofficeUsersUseCase listBackofficeUsersUseCase;
    private GetUserActivitiesUseCase getUserActivitiesUseCase;
    private GetUserAiDiagnosticsUseCase getUserAiDiagnosticsUseCase;
    private GetUserFinancialsUseCase getUserFinancialsUseCase;
    private GetUserAntiScrollStatsUseCase getUserAntiScrollStatsUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listBackofficeUsersUseCase = mock(ListBackofficeUsersUseCase.class);
        getUserActivitiesUseCase = mock(GetUserActivitiesUseCase.class);
        getUserAiDiagnosticsUseCase = mock(GetUserAiDiagnosticsUseCase.class);
        getUserFinancialsUseCase = mock(GetUserFinancialsUseCase.class);
        getUserAntiScrollStatsUseCase = mock(GetUserAntiScrollStatsUseCase.class);

        AdminUserController controller = new AdminUserController(
                listBackofficeUsersUseCase,
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
    @DisplayName("Devuelve 200 con la lista de usuarios de backoffice")
    void getBackofficeUsersShouldReturnList() throws Exception {
        givenBackofficeUsers(fullUserSummary());

        ResultActions result = performGetUsers();

        thenOkWithFullUser(result);
    }

    @Test
    @DisplayName("Pasa el parámetro de búsqueda al use case")
    void getBackofficeUsersWithSearchParamShouldPassSearchParamToUseCase() throws Exception {
        givenSearchReturnsEmpty("alice");

        ResultActions result = performGetUsersWithSearch("alice");

        thenOk(result);
        thenUsersSearchedWith("alice");
    }

    @Test
    @DisplayName("Devuelve 200 manejando valores nulos del usuario")
    void getBackofficeUsersShouldHandleNulls() throws Exception {
        givenBackofficeUsers(userSummaryWithNulls());

        ResultActions result = performGetUsers();

        thenOkWithUserHandlingNulls(result);
    }

    @Test
    @DisplayName("Devuelve 200 con las actividades del usuario")
    void getUserActivitiesShouldReturnActivities() throws Exception {
        givenUserActivities(userActivitiesResult());

        ResultActions result = performGetUserActivities();

        thenOkWithUserActivities(result);
    }

    @Test
    @DisplayName("Pasa el timeframe convertido al use case de actividades")
    void getUserActivitiesWithSpecificTimeframeShouldPassConvertedTimeframeToUseCase() throws Exception {
        givenUserActivitiesForWeek(emptyUserActivitiesResult());

        ResultActions result = performGetUserActivitiesWithTimeframe("week");

        thenOk(result);
        thenUserActivitiesQueriedForWeek();
    }

    @Test
    @DisplayName("Devuelve 500 cuando el timeframe es inválido")
    void getUserActivitiesWithInvalidTimeframeShouldReturnInternalServerError() throws Exception {
        ResultActions result = performGetUserActivitiesWithTimeframe("invalid");

        thenInternalServerError(result);
    }

    @Test
    @DisplayName("Devuelve 200 con el diagnóstico de IA del usuario")
    void getUserAiDiagnosticsShouldReturnAiDiagnostics() throws Exception {
        givenUserAiDiagnostics();

        ResultActions result = performGetUserAiDiagnostics();

        thenOkWithAiDiagnostics(result);
    }

    @Test
    @DisplayName("Devuelve 200 con las finanzas del usuario")
    void getUserFinancialsShouldReturnFinancials() throws Exception {
        givenUserFinancials();

        ResultActions result = performGetUserFinancials();

        thenOkWithFinancials(result);
    }

    @Test
    @DisplayName("Devuelve 200 con las estadísticas anti-scroll del usuario")
    void getUserAntiScrollStatsShouldReturnStats() throws Exception {
        givenUserAntiScrollStats();

        ResultActions result = performGetUserAntiScrollStats();

        thenOkWithAntiScrollStats(result);
    }

    // --- arrange ---
    private void givenBackofficeUsers(BackofficeUserSummary summary) {
        when(listBackofficeUsersUseCase.execute(any())).thenReturn(List.of(summary));
    }

    private void givenSearchReturnsEmpty(String search) {
        when(listBackofficeUsersUseCase.execute(search)).thenReturn(List.of());
    }



    private void givenUserActivities(GetUserActivitiesResponse result) {
        when(getUserActivitiesUseCase.execute(any())).thenReturn(result);
    }

    private void givenUserActivitiesForWeek(GetUserActivitiesResponse result) {
        when(getUserActivitiesUseCase.execute(eq(new GetUserActivitiesRequest(2L, Timeframe.WEEK)))).thenReturn(result);
    }

    private void givenUserAiDiagnostics() {
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
    }

    private void givenUserFinancials() {
        com.huly.backend.domain.useCase.admin.userFinancials.PaymentEventResponse payment =
                new com.huly.backend.domain.useCase.admin.userFinancials.PaymentEventResponse(
                        100L, 200L, "product", java.math.BigDecimal.TEN, "ref", 300L, "SUCCESS", 100, "COIN_PACK", java.time.Instant.now()
                );
        GetUserFinancialsResponse result = new GetUserFinancialsResponse(java.util.List.of(payment), java.math.BigDecimal.TEN);
        when(getUserFinancialsUseCase.execute(any())).thenReturn(result);
    }

    private void givenUserAntiScrollStats() {
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
    }

    private BackofficeUserSummary fullUserSummary() {
        return BackofficeUserSummary.builder()
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
    }

    private BackofficeUserSummary userSummaryWithNulls() {
        return BackofficeUserSummary.builder()
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
    }

    private GetUserActivitiesResponse userActivitiesResult() {
        ActivitySessionResponse session =
                new ActivitySessionResponse(1L, "BREATHING", "Respiración guiada", java.time.Instant.now());
        return new GetUserActivitiesResponse(
                java.util.List.of(session),
                5L,
                "BREATHING",
                "1.5",
                java.util.Map.of("BREATHING", 5),
                java.util.Map.of("BREATHING", "Respiración guiada")
        );
    }

    private GetUserActivitiesResponse emptyUserActivitiesResult() {
        return new GetUserActivitiesResponse(java.util.List.of(), 5L, null, null, null, java.util.Map.of());
    }



    // --- act ---
    private ResultActions performGetUsers() throws Exception {
        return mockMvc.perform(get("/api/admin/users"));
    }

    private ResultActions performGetUsersWithSearch(String search) throws Exception {
        return mockMvc.perform(get("/api/admin/users").param("search", search));
    }



    private ResultActions performGetUserActivities() throws Exception {
        return mockMvc.perform(get("/api/admin/users/2/statistics/activities"));
    }

    private ResultActions performGetUserActivitiesWithTimeframe(String timeframe) throws Exception {
        return mockMvc.perform(get("/api/admin/users/2/statistics/activities").param("timeframe", timeframe));
    }

    private ResultActions performGetUserAiDiagnostics() throws Exception {
        return mockMvc.perform(get("/api/admin/users/2/statistics/ai"));
    }

    private ResultActions performGetUserFinancials() throws Exception {
        return mockMvc.perform(get("/api/admin/users/2/statistics/finance"));
    }

    private ResultActions performGetUserAntiScrollStats() throws Exception {
        return mockMvc.perform(get("/api/admin/users/2/statistics/antiscroll"));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenOkWithFullUser(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
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

    private void thenOkWithUserHandlingNulls(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
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

    private void thenUsersSearchedWith(String search) {
        verify(listBackofficeUsersUseCase).execute(search);
    }

    private void thenUserActivitiesQueriedForWeek() {
        verify(getUserActivitiesUseCase).execute(new GetUserActivitiesRequest(2L, Timeframe.WEEK));
    }

    private void thenInternalServerError(ResultActions result) throws Exception {
        result.andExpect(status().isInternalServerError());
    }

    private void thenOkWithUserActivities(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.todayActivitiesCount").value(5))
                .andExpect(jsonPath("$.activitySessions[0].id").value(1))
                .andExpect(jsonPath("$.activitySessions[0].activityType").value("BREATHING"))
                .andExpect(jsonPath("$.activitySessions[0].activityName").value("Respiración guiada"))
                .andExpect(jsonPath("$.favoriteActivity").value("BREATHING"))
                .andExpect(jsonPath("$.averageSessionsText").value("1.5"))
                .andExpect(jsonPath("$.activityDistribution.BREATHING").value(5))
                .andExpect(jsonPath("$.activityNames.BREATHING").value("Respiración guiada"));
    }

    private void thenOkWithAiDiagnostics(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.aiMemories[0].id").value("mem-1"))
                .andExpect(jsonPath("$.aiMemories[0].content").value("content"))
                .andExpect(jsonPath("$.emotionalEvents[0].id").value(10))
                .andExpect(jsonPath("$.emotionalEvents[0].inputText").value("inputText"))
                .andExpect(jsonPath("$.preferredName").value("Alice"))
                .andExpect(jsonPath("$.receptivityScore").value(80))
                .andExpect(jsonPath("$.receptivityLabel").value("HIGH"))
                .andExpect(jsonPath("$.dominantEmotion").value("JOY"));
    }

    private void thenOkWithFinancials(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentEvents[0].id").value(100))
                .andExpect(jsonPath("$.paymentEvents[0].productName").value("product"))
                .andExpect(jsonPath("$.totalEarnings").value(10));
    }

    private void thenOkWithAntiScrollStats(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
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
