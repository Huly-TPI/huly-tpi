package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.BreathingSession.BreathingTechniqueItem;
import com.huly.backend.domain.dto.BreathingSession.GetBreathingTechniquesResponse;
import com.huly.backend.domain.useCase.BreathingSession.GetBreathingTechniquesUseCase;
import com.huly.backend.infrastructure.presentation.mapper.breathing.BreathingPresentationMapper;
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
        BreathingController controller = new BreathingController(getBreathingTechniquesUseCase,
                new BreathingPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllBreathingTechniques_shouldReturnListOfTechniques() throws Exception {
        GetBreathingTechniquesResponse techniques = new GetBreathingTechniquesResponse(List.of(
                new BreathingTechniqueItem(1L, "Diafragmática", null, 4, 0, 4, 1, 4),
                new BreathingTechniqueItem(2L, "Cuadrada", null, 4, 4, 4, 1, 4)
        ));
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
        when(getBreathingTechniquesUseCase.execute())
                .thenReturn(new GetBreathingTechniquesResponse(List.of()));

        mockMvc.perform(get("/api/breathing/techniques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
