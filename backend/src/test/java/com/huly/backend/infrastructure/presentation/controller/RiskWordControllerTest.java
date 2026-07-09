package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.dto.riskWord.DeleteRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.dto.riskWord.RiskWordItem;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordRequest;
import com.huly.backend.infrastructure.presentation.mapper.riskWord.RiskWordPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RiskWordControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateRiskWordUseCase createRiskWordUseCase;
    private UpdateRiskWordUseCase updateRiskWordUseCase;
    private DeleteRiskWordUseCase deleteRiskWordUseCase;
    private ListRiskWordsUseCase listRiskWordsUseCase;

    @BeforeEach
    void setUp() {
        createRiskWordUseCase = mock(CreateRiskWordUseCase.class);
        updateRiskWordUseCase = mock(UpdateRiskWordUseCase.class);
        deleteRiskWordUseCase = mock(DeleteRiskWordUseCase.class);
        listRiskWordsUseCase = mock(ListRiskWordsUseCase.class);

        RiskWordController controller = new RiskWordController(
                createRiskWordUseCase, updateRiskWordUseCase,
                deleteRiskWordUseCase, listRiskWordsUseCase,
                new RiskWordPresentationMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve 201 con la palabra de riesgo cuando el request es válido")
    void createShouldReturn201WithRiskWordResponseWhenRequestIsValid() throws Exception {
        givenCreateReturnsForValidWord();
        String body = riskWordRequestJson("suicidio", "desc", RiskSeverity.HIGH);

        ResultActions result = performCreate(body);

        thenCreatedWithRiskWord(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la palabra está vacía")
    void createShouldReturn400WhenWordIsBlank() throws Exception {
        String body = riskWordRequestJson("", "desc", RiskSeverity.HIGH);

        ResultActions result = performCreate(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la palabra de riesgo actualizada cuando es válida")
    void updateShouldReturn200WithUpdatedRiskWordWhenValid() throws Exception {
        givenUpdateReturnsForValidWord();
        String body = riskWordRequestJson("panico", null, RiskSeverity.MEDIUM);

        ResultActions result = performUpdate(body);

        thenOkWithUpdatedRiskWord(result);
    }

    @Test
    @DisplayName("Devuelve 204 cuando el id existe")
    void deleteShouldReturn204WhenIdExists() throws Exception {
        ResultActions result = performDelete();

        thenNoContent(result);
        thenDeleteWasInvoked();
    }

    @Test
    @DisplayName("Devuelve 200 con la página de palabras de riesgo")
    void listShouldReturn200WithRiskWordPageResponse() throws Exception {
        givenListReturnsSinglePage();

        ResultActions result = performList();

        thenOkWithSinglePage(result);
    }

    @Test
    @DisplayName("Devuelve 200 con contenido vacío cuando se aplican filtros")
    void listShouldReturn200WithEmptyContentWhenFiltersApplied() throws Exception {
        givenListReturnsEmptyForFilters();

        ResultActions result = performListWithFilters();

        thenOkWithZeroTotalAndEmptyContent(result);
    }

    @Test
    @DisplayName("Serializa severity nula cuando el dominio no tiene severidad")
    void createShouldSerializeNullSeverityWhenDomainHasNoSeverity() throws Exception {
        givenCreateReturnsNullSeverity();
        String body = riskWordRequestJson("estres", null, RiskSeverity.LOW);

        ResultActions result = performCreate(body);

        thenCreatedWithNullSeverity(result);
    }

    @Test
    @DisplayName("Devuelve 200 cuando la severidad está vacía")
    void listShouldReturn200WhenSeverityIsBlank() throws Exception {
        givenListReturnsEmptyForBlankSeverity();

        ResultActions result = performListWithBlankSeverity();

        thenOkWithEmptyContent(result);
    }

    // --- arrange ---
    private void givenCreateReturnsForValidWord() {
        CreateRiskWordResponse saved = new CreateRiskWordResponse(1L, "suicidio", "desc", RiskSeverity.HIGH, true);
        when(createRiskWordUseCase.execute(new CreateRiskWordRequest("suicidio", "desc", RiskSeverity.HIGH)))
                .thenReturn(saved);
    }

    private void givenCreateReturnsNullSeverity() {
        CreateRiskWordResponse saved = new CreateRiskWordResponse(2L, "estres", null, null, true);
        when(createRiskWordUseCase.execute(any(CreateRiskWordRequest.class))).thenReturn(saved);
    }

    private void givenUpdateReturnsForValidWord() {
        UpdateRiskWordResponse updated = new UpdateRiskWordResponse(1L, "panico", null, RiskSeverity.MEDIUM, true);
        when(updateRiskWordUseCase.execute(new UpdateRiskWordRequest(1L, "panico", null, RiskSeverity.MEDIUM)))
                .thenReturn(updated);
    }

    private void givenListReturnsSinglePage() {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(new RiskWordItem(1L, "suicidio", null, RiskSeverity.HIGH, true)),
                0, 20, 1L, 1, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest(null, null, null, 0, 20)))
                .thenReturn(response);
    }

    private void givenListReturnsEmptyForFilters() {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(), 0, 20, 0L, 0, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest("suicidio", true, "HIGH", 0, 20)))
                .thenReturn(response);
    }

    private void givenListReturnsEmptyForBlankSeverity() {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(), 0, 20, 0L, 0, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest(null, null, "", 0, 20)))
                .thenReturn(response);
    }

    private String riskWordRequestJson(String word, String description, RiskSeverity severity) throws Exception {
        return objectMapper.writeValueAsString(new RiskWordRequest(word, description, severity));
    }

    // --- act ---
    private ResultActions performCreate(String body) throws Exception {
        return mockMvc.perform(post("/api/risk-words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performUpdate(String body) throws Exception {
        return mockMvc.perform(put("/api/risk-words/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performDelete() throws Exception {
        return mockMvc.perform(delete("/api/risk-words/1"));
    }

    private ResultActions performList() throws Exception {
        return mockMvc.perform(get("/api/risk-words"));
    }

    private ResultActions performListWithFilters() throws Exception {
        return mockMvc.perform(get("/api/risk-words")
                .param("word", "suicidio")
                .param("active", "true")
                .param("severity", "HIGH"));
    }

    private ResultActions performListWithBlankSeverity() throws Exception {
        return mockMvc.perform(get("/api/risk-words").param("severity", ""));
    }

    // --- assert ---
    private void thenCreatedWithRiskWord(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.word").value("suicidio"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.active").value(true));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenOkWithUpdatedRiskWord(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.word").value("panico"))
                .andExpect(jsonPath("$.severity").value("MEDIUM"));
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenOkWithSinglePage(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].word").value("suicidio"))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    private void thenOkWithZeroTotalAndEmptyContent(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private void thenCreatedWithNullSeverity(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity", nullValue()));
    }

    private void thenOkWithEmptyContent(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private void thenDeleteWasInvoked() {
        verify(deleteRiskWordUseCase).execute(new DeleteRiskWordRequest(1L));
    }
}
