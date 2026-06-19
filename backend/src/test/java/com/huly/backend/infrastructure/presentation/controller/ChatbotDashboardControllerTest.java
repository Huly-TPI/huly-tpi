package com.huly.backend.infrastructure.presentation.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
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
    void getEmotionalCategories_shouldReturnMockList() throws Exception {
        mockMvc.perform(get("/api/admin/chatbot/emotional-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Estrés"));
    }

    @Test
    void getActivities_shouldReturnMockList() throws Exception {
        mockMvc.perform(get("/api/admin/chatbot/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Reventar burbujas"));
    }

    @Test
    void getWellbeing_shouldReturnMockData() throws Exception {
        mockMvc.perform(get("/api/admin/chatbot/wellbeing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").isArray())
                .andExpect(jsonPath("$.labels").isArray());
    }

    @Test
    void getTrainingLogs_shouldReturnMockList() throws Exception {
        mockMvc.perform(get("/api/admin/chatbot/training-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
