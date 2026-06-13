package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.admin.TopAppStats;
import com.huly.backend.domain.useCase.admin.GetAntiScrollDashboardUseCase;
import com.huly.backend.domain.useCase.admin.ListBackofficeUsersUseCase;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listBackofficeUsersUseCase = mock(ListBackofficeUsersUseCase.class);
        getAntiScrollDashboardUseCase = mock(GetAntiScrollDashboardUseCase.class);
        antiScrollConfigRepository = mock(com.huly.backend.domain.repository.extension.AntiScrollConfigRepository.class);

        when(antiScrollConfigRepository.findFirst()).thenReturn(java.util.Optional.empty());

        AdminUserController controller = new AdminUserController(
                listBackofficeUsersUseCase,
                getAntiScrollDashboardUseCase,
                antiScrollConfigRepository
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
                .build();

        when(listBackofficeUsersUseCase.execute()).thenReturn(List.of(summary));

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
                .andExpect(jsonPath("$[0].totalScrollTimeSeconds").value(5000));
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
    void updateAntiScrollConfig_shouldSaveConfig() throws Exception {
        com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest request = new com.huly.backend.infrastructure.presentation.dto.admin.AntiScrollConfigRequest(15, "nuevos terminos");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/antiscroll/config")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(antiScrollConfigRepository).save(any());
    }
}
