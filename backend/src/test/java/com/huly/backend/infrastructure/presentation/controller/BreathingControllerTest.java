package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BreathingControllerTest {

    private MockMvc mockMvc;
    private GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;

    @BeforeEach
    void setUp() {
        getBreathingTechniquesUseCase = mock(GetBreathingTechniquesUseCase.class);
        BreathingController controller = new BreathingController(getBreathingTechniquesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllBreathingTechniques_shouldReturnListOfTechniques() throws Exception {
        List<BreathingTechnique> techniques = List.of(
                BreathingTechnique.builder().id(1L).name("Diafragmática").inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4).build(),
                BreathingTechnique.builder().id(2L).name("Cuadrada").inhaleSeconds(4).holdSeconds(4).exhaleSeconds(4).roundsInterval(1).rounds(4).build()
        );
        when(getBreathingTechniquesUseCase.execute()).thenReturn(techniques);

        mockMvc.perform(get("/api/breathing/techniques"))
                .andExpect(status().isOk())
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

    @Test
    void getAllBreathingTechniques_shouldReturnEmptyList_whenNoTechniques() throws Exception {
        when(getBreathingTechniquesUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/breathing/techniques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
