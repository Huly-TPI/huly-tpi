package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.admin.dashboard.GetAdminDashboardResponse;
import com.huly.backend.domain.useCase.admin.dashboard.GetAdminDashboardUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.AdminPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDashboardControllerTest {

    private MockMvc mockMvc;
    private GetAdminDashboardUseCase getAdminDashboardUseCase;

    @BeforeEach
    void setUp() {
        getAdminDashboardUseCase = mock(GetAdminDashboardUseCase.class);
        AdminDashboardController controller = new AdminDashboardController(
                getAdminDashboardUseCase,
                new AdminPresentationMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Devuelve 200 con las estadísticas del dashboard")
    void getDashboardStatsShouldReturnStats() throws Exception {
        givenDashboardStats();

        ResultActions result = performGetDashboard();

        thenOkWithDashboardStats(result);
    }

    private void givenDashboardStats() {
        GetAdminDashboardResponse stats = GetAdminDashboardResponse.builder()
                .activeExtensionUsersCount(30)
                .usersRegisteredThisWeek(10)
                .activitiesThisWeek(25)
                .build();
        when(getAdminDashboardUseCase.execute()).thenReturn(stats);
    }

    private ResultActions performGetDashboard() throws Exception {
        return mockMvc.perform(get("/api/admin/dashboard"));
    }

    private void thenOkWithDashboardStats(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.activeExtensionUsersCount").value(30))
                .andExpect(jsonPath("$.usersRegisteredThisWeek").value(10))
                .andExpect(jsonPath("$.activitiesThisWeek").value(25));
    }
}
