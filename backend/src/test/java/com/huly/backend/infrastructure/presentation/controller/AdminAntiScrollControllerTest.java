package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.useCase.admin.antiscroll.GetAntiScrollDashboardResponse;
import com.huly.backend.domain.useCase.admin.antiscroll.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigResponse;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.GetAntiScrollGlobalConfigUseCase;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigRequest;
import com.huly.backend.domain.useCase.admin.antiScrollConfig.UpdateAntiScrollGlobalConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAntiScrollControllerTest {

    private static final String DEFAULT_TERMS = "El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!";

    private MockMvc mockMvc;
    private GetAntiScrollDashboardUseCase getAntiScrollDashboardUseCase;
    private GetAntiScrollGlobalConfigUseCase getAntiScrollGlobalConfigUseCase;
    private UpdateAntiScrollGlobalConfigUseCase updateAntiScrollGlobalConfigUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        getAntiScrollDashboardUseCase = mock(GetAntiScrollDashboardUseCase.class);
        getAntiScrollGlobalConfigUseCase = mock(GetAntiScrollGlobalConfigUseCase.class);
        updateAntiScrollGlobalConfigUseCase = mock(UpdateAntiScrollGlobalConfigUseCase.class);

        when(getAntiScrollGlobalConfigUseCase.execute()).thenReturn(new GetAntiScrollGlobalConfigResponse(
                20,
                DEFAULT_TERMS
        ));

        AdminAntiScrollController controller = new AdminAntiScrollController(
                getAntiScrollDashboardUseCase,
                getAntiScrollGlobalConfigUseCase,
                updateAntiScrollGlobalConfigUseCase,
                new AdminPresentationMapper()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Devuelve 200 con las estadísticas de antiscroll dashboard")
    void getAntiScrollDashboardStatsShouldReturnStats() throws Exception {
        givenAntiScrollDashboardStats();

        ResultActions result = performGetAntiScrollDashboard();

        thenOkWithAntiScrollDashboardStats(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la configuración global de antiscroll")
    void getAntiScrollGlobalConfigShouldReturnConfig() throws Exception {
        givenGlobalConfig(25, "terminos de prueba");

        ResultActions result = performGetConfig();

        thenOkWithConfig(result, 25, "terminos de prueba");
    }

    @Test
    @DisplayName("Devuelve 200 con la configuración global por defecto")
    void getAntiScrollGlobalConfigShouldReturnDefaultConfig() throws Exception {
        ResultActions result = performGetConfig();

        thenOkWithConfig(result, 20, DEFAULT_TERMS);
    }

    @Test
    @DisplayName("Guarda la configuración global anti-scroll")
    void updateAntiScrollGlobalConfigShouldSaveConfig() throws Exception {
        String body = antiScrollConfigRequestJson(15, "nuevos terminos");

        ResultActions result = performUpdateConfig(body);

        thenOk(result);
        thenConfigUpdatedWith(15, "nuevos terminos");
    }

    private void givenAntiScrollDashboardStats() {
        GetAntiScrollDashboardResponse stats = GetAntiScrollDashboardResponse.builder()
                .totalModalsShown(100)
                .totalRedirects(40)
                .totalUsersCount(50)
                .activeExtensionUsersCount(30)
                .dataSharingConsentUsersCount(20)
                .topUsedApps(List.of(new TopAppStats("instagram.com", 3600)))
                .build();
        when(getAntiScrollDashboardUseCase.execute()).thenReturn(stats);
    }

    private void givenGlobalConfig(int minutes, String terms) {
        when(getAntiScrollGlobalConfigUseCase.execute())
                .thenReturn(new GetAntiScrollGlobalConfigResponse(minutes, terms));
    }

    private String antiScrollConfigRequestJson(int minutes, String terms) throws Exception {
        return objectMapper.writeValueAsString(new AntiScrollConfigRequest(minutes, terms));
    }

    private ResultActions performGetAntiScrollDashboard() throws Exception {
        return mockMvc.perform(get("/api/admin/antiscroll/dashboard"));
    }

    private ResultActions performGetConfig() throws Exception {
        return mockMvc.perform(get("/api/admin/antiscroll/config"));
    }

    private ResultActions performUpdateConfig(String body) throws Exception {
        return mockMvc.perform(post("/api/admin/antiscroll/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenOkWithAntiScrollDashboardStats(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalModalsShown").value(100))
                .andExpect(jsonPath("$.totalRedirects").value(40))
                .andExpect(jsonPath("$.totalUsersCount").value(50))
                .andExpect(jsonPath("$.activeExtensionUsersCount").value(30))
                .andExpect(jsonPath("$.dataSharingConsentUsersCount").value(20))
                .andExpect(jsonPath("$.topUsedApps[0].domain").value("instagram.com"))
                .andExpect(jsonPath("$.topUsedApps[0].totalActiveSeconds").value(3600));
    }

    private void thenOkWithConfig(ResultActions result, int minutes, String terms) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultPauseIntervalMinutes").value(minutes))
                .andExpect(jsonPath("$.termsAndConditions").value(terms));
    }

    private void thenConfigUpdatedWith(int minutes, String terms) {
        verify(updateAntiScrollGlobalConfigUseCase).execute(new UpdateAntiScrollGlobalConfigRequest(minutes, terms));
    }
}
