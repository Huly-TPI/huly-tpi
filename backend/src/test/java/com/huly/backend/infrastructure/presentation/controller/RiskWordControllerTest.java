package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.useCase.riskWord.CreateRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.DeleteRiskWordUseCase;
import com.huly.backend.domain.useCase.riskWord.ListRiskWordsUseCase;
import com.huly.backend.domain.useCase.riskWord.UpdateRiskWordUseCase;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                deleteRiskWordUseCase, listRiskWordsUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldReturn201WithRiskWordResponse_whenRequestIsValid() throws Exception {
        RiskWord saved = RiskWord.builder().id(1L).word("suicidio").description("desc").severity(RiskSeverity.HIGH).active(true).build();
        when(createRiskWordUseCase.execute("suicidio", "desc", RiskSeverity.HIGH)).thenReturn(saved);

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
        RiskWord updated = RiskWord.builder().id(1L).word("panico").severity(RiskSeverity.MEDIUM).active(true).build();
        when(updateRiskWordUseCase.execute(eq(1L), eq("panico"), isNull(), eq(RiskSeverity.MEDIUM))).thenReturn(updated);

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

        verify(deleteRiskWordUseCase).execute(1L);
    }

    @Test
    void list_shouldReturn200WithRiskWordPageResponse() throws Exception {
        RiskWord word = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        Page<RiskWord> page = new PageImpl<>(List.of(word));
        when(listRiskWordsUseCase.execute(isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

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
        Page<RiskWord> page = new PageImpl<>(List.of());
        when(listRiskWordsUseCase.execute(eq("suicidio"), eq(true), eq("HIGH"), any(Pageable.class))).thenReturn(page);

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
        RiskWord saved = RiskWord.builder().id(2L).word("estres").severity(null).active(true).build();
        when(createRiskWordUseCase.execute(any(), any(), any())).thenReturn(saved);

        RiskWordRequest request = new RiskWordRequest("estres", null, RiskSeverity.LOW);

        mockMvc.perform(post("/api/risk-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severity", nullValue()));
    }

    @Test
    void list_shouldReturn200_whenSeverityIsBlank() throws Exception {
        Page<RiskWord> page = new PageImpl<>(List.of());
        when(listRiskWordsUseCase.execute(isNull(), isNull(), eq(""), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/risk-words").param("severity", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
