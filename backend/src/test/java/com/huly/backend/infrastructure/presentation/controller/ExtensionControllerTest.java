package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.domain.useCase.extension.GetUserAntiScrollSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveUserAntiScrollSettingsUseCase;
import com.huly.backend.infrastructure.presentation.dto.extension.AntiScrollSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.extension.ExtensionPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExtensionControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase;
    private SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase;
    private SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        getUserAntiScrollSettingsUseCase = mock(GetUserAntiScrollSettingsUseCase.class);
        saveUserAntiScrollSettingsUseCase = mock(SaveUserAntiScrollSettingsUseCase.class);
        saveExtensionMetricsUseCase = mock(SaveExtensionMetricsUseCase.class);
        ExtensionController controller = new ExtensionController(
                getUserAntiScrollSettingsUseCase,
                saveUserAntiScrollSettingsUseCase,
                saveExtensionMetricsUseCase,
                new ExtensionPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 con los settings y el nombre de usuario")
    void getSettingsShouldReturn200WithSettingsAndUserName() throws Exception {
        // --- arrange ---
        givenSettings(antiScrollSettings());

        // --- act ---
        ResultActions result = performGetSettings();

        // --- assert ---
        thenOkWithSettings(result);
    }

    @Test
    @DisplayName("Devuelve 200 cuando el request de settings es valido")
    void saveSettingsShouldReturn200WhenRequestIsValid() throws Exception {
        // --- act ---
        ResultActions result = performSaveSettings(validSettingsRequest());

        // --- assert ---
        thenOk(result);
        thenSettingsSaved();
    }

    @Test
    @DisplayName("Devuelve 200 cuando el request de metricas es valido")
    void saveMetricsShouldReturn200WhenRequestIsValid() throws Exception {
        // --- act ---
        ResultActions result = performSaveMetrics(List.of(validMetricRequest()));

        // --- assert ---
        thenOk(result);
        thenMetricsSaved();
    }

    @Test
    @DisplayName("Devuelve 401 cuando no esta autenticado")
    void getSettingsShouldReturn401WhenNotAuthenticated() throws Exception {
        // --- arrange ---
        givenNoAuthentication();

        // --- act ---
        ResultActions result = performGetSettings();

        // --- assert ---
        thenUnauthorized(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenSettings(GetUserAntiScrollSettingsResponse settings) {
        when(getUserAntiScrollSettingsUseCase.execute(argThat(req -> req.userId().equals(USER_ID))))
                .thenReturn(settings);
    }

    private GetUserAntiScrollSettingsResponse antiScrollSettings() {
        return GetUserAntiScrollSettingsResponse.builder()
                .enabled(true)
                .pauseIntervalSeconds(15)
                .gardenUrl("http://localhost:5173/")
                .backendUrl("http://localhost:8080")
                .monitoredDomains(List.of("twitter.com", "x.com"))
                .dataSharingConsent(true)
                .userName("Jim")
                .termsAndConditions("dynamic terms")
                .build();
    }

    private AntiScrollSettingsRequest validSettingsRequest() {
        AntiScrollSettingsRequest request = new AntiScrollSettingsRequest();
        request.setEnabled(true);
        request.setPauseIntervalSeconds(20);
        request.setMonitoredDomains(List.of("tiktok.com"));
        request.setDataSharingConsent(true);
        return request;
    }

    private ExtensionMetricRequest validMetricRequest() {
        ExtensionMetricRequest metricRequest = new ExtensionMetricRequest();
        metricRequest.setDomain("instagram.com");
        metricRequest.setActiveSeconds(30);
        metricRequest.setScrollCount(2);
        metricRequest.setModalsShown(0);
        metricRequest.setRedirects(0);
        return metricRequest;
    }

    // --- act ---
    private ResultActions performGetSettings() throws Exception {
        return mockMvc.perform(get("/api/extension/settings"));
    }

    private ResultActions performSaveSettings(AntiScrollSettingsRequest request) throws Exception {
        return mockMvc.perform(post("/api/extension/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performSaveMetrics(List<ExtensionMetricRequest> requests) throws Exception {
        return mockMvc.perform(post("/api/extension/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenOkWithSettings(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.pauseIntervalSeconds").value(15))
                .andExpect(jsonPath("$.gardenUrl").value("http://localhost:5173/"))
                .andExpect(jsonPath("$.userName").value("Jim"))
                .andExpect(jsonPath("$.termsAndConditions").value("dynamic terms"))
                .andExpect(jsonPath("$.dataSharingConsent").value(true));
    }

    private void thenSettingsSaved() {
        verify(saveUserAntiScrollSettingsUseCase).execute(argThat((SaveUserAntiScrollSettingsRequest req) ->
                req.userId().equals(USER_ID) && req.enabled() && req.pauseIntervalSeconds() == 20));
    }

    private void thenMetricsSaved() {
        verify(saveExtensionMetricsUseCase).execute(argThat((SaveExtensionMetricsRequest req) ->
                req.userId().equals(USER_ID) && req.metrics().size() == 1));
    }
}
