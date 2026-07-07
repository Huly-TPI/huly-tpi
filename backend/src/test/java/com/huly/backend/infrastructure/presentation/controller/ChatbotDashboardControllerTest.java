package com.huly.backend.infrastructure.presentation.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotDashboardControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatbotDashboardController controller = new ChatbotDashboardController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve 200 con la lista simulada de categorías emocionales")
    void getEmotionalCategoriesShouldReturnMockList() throws Exception {
        ResultActions result = performGetEmotionalCategories();

        thenOkWithFirstItemName(result, "Estrés");
    }

    @Test
    @DisplayName("Devuelve 200 con la lista simulada de actividades")
    void getActivitiesShouldReturnMockList() throws Exception {
        ResultActions result = performGetActivities();

        thenOkWithFirstItemName(result, "Reventar burbujas");
    }

    @Test
    @DisplayName("Devuelve 200 con los datos simulados de bienestar")
    void getWellbeingShouldReturnMockData() throws Exception {
        ResultActions result = performGetWellbeing();

        thenOkWithWellbeingSeries(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la lista simulada de logs de entrenamiento")
    void getTrainingLogsShouldReturnMockList() throws Exception {
        ResultActions result = performGetTrainingLogs();

        thenOkWithArray(result);
    }

    // --- act ---
    private ResultActions performGetEmotionalCategories() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/emotional-categories"));
    }

    private ResultActions performGetActivities() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/activities"));
    }

    private ResultActions performGetWellbeing() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/wellbeing"));
    }

    private ResultActions performGetTrainingLogs() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/training-logs"));
    }

    // --- assert ---
    private void thenOkWithFirstItemName(ResultActions result, String name) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value(name));
    }

    private void thenOkWithWellbeingSeries(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isArray())
                .andExpect(jsonPath("$.labels").isArray());
    }

    private void thenOkWithArray(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
