package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.useCase.breathingTechnique.CreateBreathingTechniqueUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.ListBreathingTechniquesUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.SetBreathingTechniqueActiveUseCase;
import com.huly.backend.domain.useCase.breathingTechnique.UpdateBreathingTechniqueUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BreathingAdminControllerTest {

    private MockMvc mockMvc;
    private ListBreathingTechniquesUseCase listUseCase;
    private CreateBreathingTechniqueUseCase createUseCase;
    private UpdateBreathingTechniqueUseCase updateUseCase;
    private SetBreathingTechniqueActiveUseCase setActiveUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        listUseCase = mock(ListBreathingTechniquesUseCase.class);
        createUseCase = mock(CreateBreathingTechniqueUseCase.class);
        updateUseCase = mock(UpdateBreathingTechniqueUseCase.class);
        setActiveUseCase = mock(SetBreathingTechniqueActiveUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BreathingAdminController(
                listUseCase, createUseCase, updateUseCase, setActiveUseCase)).build();
    }

    @Test
    @DisplayName("Lista las técnicas de respiración")
    void listShouldReturnTechniques() throws Exception {
        givenListReturns(technique(1L));

        ResultActions result = performList();

        thenTechniqueListed(result);
    }

    @Test
    @DisplayName("Crea una técnica y responde 201")
    void createShouldReturn201() throws Exception {
        givenCreateReturns(technique(7L));

        ResultActions result = performCreate(validBody());

        thenCreatedWithId(result, 7);
    }

    @Test
    @DisplayName("Actualiza una técnica y responde 200")
    void updateShouldReturn200() throws Exception {
        givenUpdateReturns(technique(5L));

        ResultActions result = performUpdate(5L, validBody());

        thenOkWithIdAndUpdateCalled(result, 5);
    }

    @Test
    @DisplayName("Cambia el estado activo y responde 200")
    void setActiveShouldReturn200() throws Exception {
        givenSetActiveReturns(9L, false, technique(9L));

        ResultActions result = performSetActive(9L, false);

        thenOkAndSetActiveCalled(result, 9L, false);
    }

    // --- arrange ---
    private void givenListReturns(BreathingTechnique... techniques) {
        when(listUseCase.execute()).thenReturn(List.of(techniques));
    }

    private void givenCreateReturns(BreathingTechnique t) {
        when(createUseCase.execute(any())).thenReturn(t);
    }

    private void givenUpdateReturns(BreathingTechnique t) {
        when(updateUseCase.execute(any())).thenReturn(t);
    }

    private void givenSetActiveReturns(Long id, boolean active, BreathingTechnique t) {
        when(setActiveUseCase.execute(eq(id), eq(active))).thenReturn(t);
    }

    private BreathingTechnique technique(Long id) {
        return BreathingTechnique.builder().id(id).name("Diafragmática").description("d")
                .inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4)
                .active(true).build();
    }

    private Map<String, Object> validBody() {
        return Map.of("name", "Diafragmática", "description", "d",
                "inhaleSeconds", 4, "holdSeconds", 0, "exhaleSeconds", 4,
                "roundsInterval", 1, "rounds", 4);
    }

    // --- act ---
    private ResultActions performList() throws Exception {
        return mockMvc.perform(get("/api/admin/breathing-techniques"));
    }

    private ResultActions performCreate(Map<String, Object> body) throws Exception {
        return mockMvc.perform(post("/api/admin/breathing-techniques")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performUpdate(Long id, Map<String, Object> body) throws Exception {
        return mockMvc.perform(put("/api/admin/breathing-techniques/" + id)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performSetActive(Long id, boolean active) throws Exception {
        return mockMvc.perform(patch("/api/admin/breathing-techniques/" + id + "/active")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("active", active))));
    }

    // --- assert ---
    private void thenTechniqueListed(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Diafragmática"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    private void thenCreatedWithId(ResultActions result, int id) throws Exception {
        result.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id));
        verify(createUseCase).execute(any());
    }

    private void thenOkWithIdAndUpdateCalled(ResultActions result, int id) throws Exception {
        result.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        verify(updateUseCase).execute(any());
    }

    private void thenOkAndSetActiveCalled(ResultActions result, Long id, boolean active) throws Exception {
        result.andExpect(status().isOk());
        verify(setActiveUseCase).execute(id, active);
    }
}