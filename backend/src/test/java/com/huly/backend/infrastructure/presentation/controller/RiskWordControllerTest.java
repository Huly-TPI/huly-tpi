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
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
    void create_shouldReturn201WithRiskWordResponse_whenRequestIsValid() throws Exception {
        CreateRiskWordResponse saved = new CreateRiskWordResponse(1L, "suicidio", "desc", RiskSeverity.HIGH, true);
        when(createRiskWordUseCase.execute(new CreateRiskWordRequest("suicidio", "desc", RiskSeverity.HIGH)))
                .thenReturn(saved);

        RiskWordRequest request = new RiskWordRequest("suicidio", "desc", RiskSeverity.HIGH);

        mockMvc.perform(post("/api/risk-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.word").value("suicidio"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void create_shouldReturn400_whenWordIsBlank() throws Exception {
        RiskWordRequest request = new RiskWordRequest("", "desc", RiskSeverity.HIGH);

        mockMvc.perform(post("/api/risk-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200WithUpdatedRiskWord_whenValid() throws Exception {
        UpdateRiskWordResponse updated = new UpdateRiskWordResponse(1L, "panico", null, RiskSeverity.MEDIUM, true);
        when(updateRiskWordUseCase.execute(new UpdateRiskWordRequest(1L, "panico", null, RiskSeverity.MEDIUM)))
                .thenReturn(updated);

        RiskWordRequest request = new RiskWordRequest("panico", null, RiskSeverity.MEDIUM);

        mockMvc.perform(put("/api/risk-words/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.word").value("panico"))
                .andExpect(jsonPath("$.severity").value("MEDIUM"));
    }

    @Test
    void delete_shouldReturn204_whenIdExists() throws Exception {
        mockMvc.perform(delete("/api/risk-words/1"))
                .andExpect(status().isNoContent());

        verify(deleteRiskWordUseCase).execute(new DeleteRiskWordRequest(1L));
    }

    @Test
    void list_shouldReturn200WithRiskWordPageResponse() throws Exception {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(new RiskWordItem(1L, "suicidio", null, RiskSeverity.HIGH, true)),
                0, 20, 1L, 1, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest(null, null, null, 0, 20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/risk-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].word").value("suicidio"))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void list_shouldReturn200WithEmptyContent_whenFiltersApplied() throws Exception {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(), 0, 20, 0L, 0, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest("suicidio", true, "HIGH", 0, 20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/risk-words")
                        .param("word", "suicidio")
                        .param("active", "true")
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void create_shouldSerializeNullSeverity_whenDomainHasNoSeverity() throws Exception {
        CreateRiskWordResponse saved = new CreateRiskWordResponse(2L, "estres", null, null, true);
        when(createRiskWordUseCase.execute(any(CreateRiskWordRequest.class))).thenReturn(saved);

        RiskWordRequest request = new RiskWordRequest("estres", null, RiskSeverity.LOW);

        mockMvc.perform(post("/api/risk-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity", nullValue()));
    }

    @Test
    void list_shouldReturn200_whenSeverityIsBlank() throws Exception {
        ListRiskWordsResponse response = new ListRiskWordsResponse(
                List.of(), 0, 20, 0L, 0, true, true);
        when(listRiskWordsUseCase.execute(new ListRiskWordsRequest(null, null, "", 0, 20)))
                .thenReturn(response);

        mockMvc.perform(get("/api/risk-words").param("severity", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
