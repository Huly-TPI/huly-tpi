package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.useCase.extension.GetExtensionSettingsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionMetricsUseCase;
import com.huly.backend.domain.useCase.extension.SaveExtensionSettingsUseCase;
import com.huly.backend.infrastructure.presentation.controller.ExtensionController;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionMetricRequest;
import com.huly.backend.infrastructure.presentation.dto.extension.ExtensionSettingsRequest;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExtensionControllerTest {

    private static final Long USER_ID = 1L;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetExtensionSettingsUseCase getExtensionSettingsUseCase;
    private SaveExtensionSettingsUseCase saveExtensionSettingsUseCase;
    private SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;
    private GetCurrentUserUseCase getCurrentUserUseCase;
    private com.huly.backend.domain.repository.extension.AntiScrollConfigRepository antiScrollConfigRepository;

    @BeforeEach
    void setUp() {
        getExtensionSettingsUseCase = mock(GetExtensionSettingsUseCase.class);
        saveExtensionSettingsUseCase = mock(SaveExtensionSettingsUseCase.class);
        saveExtensionMetricsUseCase = mock(SaveExtensionMetricsUseCase.class);
        getCurrentUserUseCase = mock(GetCurrentUserUseCase.class);
        antiScrollConfigRepository = mock(com.huly.backend.domain.repository.extension.AntiScrollConfigRepository.class);

        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        ExtensionController controller = new ExtensionController(
                getExtensionSettingsUseCase,
                saveExtensionSettingsUseCase,
                saveExtensionMetricsUseCase,
                getCurrentUserUseCase,
                antiScrollConfigRepository
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
    void getSettings_shouldReturn200WithSettingsAndUserName() throws Exception {
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(15)
                .gardenUrl("http://localhost:5173/")
                .backendUrl("http://localhost:8080")
                .monitoredDomains(List.of("twitter.com", "x.com"))
                .dataSharingConsent(true)
                .build();

        AppUser user = AppUser.builder().id(USER_ID).name("Jim").role(UserRole.USER).status(UserStatus.ACTIVE).build();
        UserProfile profile = new UserProfile(user, true, true, true);

        when(getExtensionSettingsUseCase.execute(USER_ID)).thenReturn(settings);
        when(getCurrentUserUseCase.execute(USER_ID)).thenReturn(profile);

        mockMvc.perform(get("/api/extension/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.pauseIntervalSeconds").value(15))
                .andExpect(jsonPath("$.gardenUrl").value("http://localhost:5173/"))
                .andExpect(jsonPath("$.userName").value("Jim"))
                .andExpect(jsonPath("$.dataSharingConsent").value(true));
    }

    @Test
    void saveSettings_shouldReturn200_whenRequestIsValid() throws Exception {
        ExtensionSettingsRequest request = new ExtensionSettingsRequest();
        request.setEnabled(true);
        request.setPauseIntervalSeconds(20);
        request.setMonitoredDomains(List.of("tiktok.com"));
        request.setDataSharingConsent(true);

        mockMvc.perform(post("/api/extension/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(saveExtensionSettingsUseCase).execute(eq(USER_ID), any(ExtensionSettings.class));
    }

    @Test
    void saveMetrics_shouldReturn200_whenRequestIsValid() throws Exception {
        ExtensionMetricRequest metricRequest = new ExtensionMetricRequest();
        metricRequest.setDomain("instagram.com");
        metricRequest.setActiveSeconds(30);
        metricRequest.setScrollCount(2);
        metricRequest.setModalsShown(0);
        metricRequest.setRedirects(0);

        mockMvc.perform(post("/api/extension/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(metricRequest))))
                .andExpect(status().isOk());

        verify(saveExtensionMetricsUseCase).execute(eq(USER_ID), anyList());
    }

    @Test
    void getSettings_shouldReturn401_whenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/extension/settings"))
                .andExpect(status().isUnauthorized());
    }
}
