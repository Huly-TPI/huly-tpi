package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.admin.chatbot.EmotionalCategoryDto;
import com.huly.backend.domain.dto.admin.chatbot.WellbeingDto;
import com.huly.backend.domain.useCase.admin.chatbot.GetEmotionalCategoriesUseCase;
import com.huly.backend.domain.useCase.admin.chatbot.GetWellbeingUseCase;
import com.huly.backend.infrastructure.presentation.mapper.chatbot.ChatbotDashboardPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotDashboardControllerTest {

    private MockMvc mockMvc;

    private GetEmotionalCategoriesUseCase getEmotionalCategoriesUseCase;
    private GetWellbeingUseCase getWellbeingUseCase;

    @BeforeEach
    void setUp() {
        getEmotionalCategoriesUseCase = mock(GetEmotionalCategoriesUseCase.class);
        getWellbeingUseCase = mock(GetWellbeingUseCase.class);

        ChatbotDashboardController controller = new ChatbotDashboardController(
                getEmotionalCategoriesUseCase,
                getWellbeingUseCase,
                new ChatbotDashboardPresentationMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve 200 con la lista de categorías emocionales")
    void getEmotionalCategoriesShouldReturnList() throws Exception {
        givenEmotionalCategories(List.of(
                new EmotionalCategoryDto("Estrés", 12, 94, "ALTA")
        ));

        ResultActions result = performGetEmotionalCategories();

        thenOkWithFirstItemName(result, "Estrés");
    }

    @Test
    @DisplayName("Devuelve 200 con los datos de bienestar")
    void getWellbeingShouldReturnData() throws Exception {
        givenWellbeing(new WellbeingDto(
                List.of(62, 58, 71, 68, 75, 65, 72),
                List.of("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        ));

        ResultActions result = performGetWellbeing();

        thenOkWithWellbeingSeries(result);
    }

    // --- arrange ---
    private void givenEmotionalCategories(List<EmotionalCategoryDto> list) {
        when(getEmotionalCategoriesUseCase.execute()).thenReturn(list);
    }

    private void givenWellbeing(WellbeingDto dto) {
        when(getWellbeingUseCase.execute()).thenReturn(dto);
    }

    // --- act ---
    private ResultActions performGetEmotionalCategories() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/emotional-categories"));
    }

    private ResultActions performGetWellbeing() throws Exception {
        return mockMvc.perform(get("/api/admin/chatbot/wellbeing"));
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
}
