package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.BreathingSession.BreathingTechniqueItem;
import com.huly.backend.domain.dto.BreathingSession.GetBreathingTechniquesResponse;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import com.huly.backend.infrastructure.presentation.mapper.breathing.BreathingPresentationMapper;
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

class BreathingControllerTest {

    private MockMvc mockMvc;
    private GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;

    @BeforeEach
    void setUp() {
        getBreathingTechniquesUseCase = mock(GetBreathingTechniquesUseCase.class);
        BreathingController controller = new BreathingController(getBreathingTechniquesUseCase,
                new BreathingPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve la lista de técnicas de respiración")
    void getAllBreathingTechniquesShouldReturnListOfTechniques() throws Exception {
        // --- arrange ---
        givenTechniques(twoTechniques());
        // --- act ---
        ResultActions result = performGetTechniques();
        // --- assert ---
        thenOkWithTwoTechniques(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no hay técnicas")
    void getAllBreathingTechniquesShouldReturnEmptyListWhenNoTechniques() throws Exception {
        // --- arrange ---
        givenTechniques(noTechniques());
        // --- act ---
        ResultActions result = performGetTechniques();
        // --- assert ---
        thenOkWithEmptyList(result);
    }

    // --- arrange ---
    private void givenTechniques(GetBreathingTechniquesResponse response) {
        when(getBreathingTechniquesUseCase.execute()).thenReturn(response);
    }

    private GetBreathingTechniquesResponse twoTechniques() {
        return new GetBreathingTechniquesResponse(List.of(
                new BreathingTechniqueItem(1L, "Diafragmática", null, 4, 0, 4, 1, 4),
                new BreathingTechniqueItem(2L, "Cuadrada", null, 4, 4, 4, 1, 4)
        ));
    }

    private GetBreathingTechniquesResponse noTechniques() {
        return new GetBreathingTechniquesResponse(List.of());
    }

    // --- act ---
    private ResultActions performGetTechniques() throws Exception {
        return mockMvc.perform(get("/api/breathing/techniques"));
    }

    // --- assert ---
    private void thenOkWithTwoTechniques(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Diafragmática"))
                .andExpect(jsonPath("$[0].inhaleSeconds").value(4))
                .andExpect(jsonPath("$[0].holdSeconds").value(0))
                .andExpect(jsonPath("$[0].exhaleSeconds").value(4))
                .andExpect(jsonPath("$[0].roundsInterval").value(1))
                .andExpect(jsonPath("$[0].rounds").value(4))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Cuadrada"))
                .andExpect(jsonPath("$[1].inhaleSeconds").value(4))
                .andExpect(jsonPath("$[1].holdSeconds").value(4))
                .andExpect(jsonPath("$[1].exhaleSeconds").value(4))
                .andExpect(jsonPath("$[1].roundsInterval").value(1))
                .andExpect(jsonPath("$[1].rounds").value(4));
    }

    private void thenOkWithEmptyList(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
