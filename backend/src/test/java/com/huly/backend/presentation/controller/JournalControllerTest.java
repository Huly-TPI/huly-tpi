package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import com.huly.backend.infrastructure.presentation.controller.JournalController;
import com.huly.backend.infrastructure.presentation.dto.journal.JournalEntryRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JournalControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CreateJournalEntryUseCase createJournalEntryUseCase;
    private ListJournalEntriesUseCase listJournalEntriesUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        createJournalEntryUseCase = mock(CreateJournalEntryUseCase.class);
        listJournalEntriesUseCase = mock(ListJournalEntriesUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        JournalController controller = new JournalController(
                createJournalEntryUseCase, listJournalEntriesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private JournalEntry buildEntry(Long id, String content, Mood mood) {
        return JournalEntry.builder()
                .id(id).userId(USER_ID).journalId(1L)
                .content(content).mood(mood).createdAt(Instant.now())
                .build();
    }

    @Test
    void create_shouldReturn201_whenRequestHasValidContentAndMood() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), eq("Hoy me sentí bien"), eq(Mood.HAPPY), anyBoolean()))
                .thenReturn(buildEntry(10L, "Hoy me sentí bien", Mood.HAPPY));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Hoy me sentí bien", "HAPPY", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("Hoy me sentí bien"))
                .andExpect(jsonPath("$.mood").value("HAPPY"));
    }

    @Test
    void create_shouldReturn201_whenMoodIsNull() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), eq("Solo escribir"), eq(null), anyBoolean()))
                .thenReturn(buildEntry(10L, "Solo escribir", null));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Solo escribir", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mood").doesNotExist());
    }

    @Test
    void create_shouldReturn201_whenMoodIsLowercase() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), any(), eq(Mood.SAD), anyBoolean()))
                .thenReturn(buildEntry(10L, "Hoy fue difícil", Mood.SAD));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Hoy fue difícil", "sad", true))))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn400_whenContentIsBlank() throws Exception {
        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("", "HAPPY", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenContentIsNull() throws Exception {
        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mood\":\"HAPPY\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenMoodIsInvalid() throws Exception {
        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Hoy fue raro", "INVALIDO", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldPassUseTextForAITrueToUseCase_whenFieldIsTrue() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), any(), any(), eq(true)))
                .thenReturn(buildEntry(10L, "Contenido", Mood.HAPPY));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Contenido", "HAPPY", true))))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldPassUseTextForAIFalseToUseCase_whenFieldIsFalse() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), any(), any(), eq(false)))
                .thenReturn(buildEntry(10L, "Contenido", Mood.HAPPY));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Contenido", "HAPPY", false))))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldDefaultToTrueForUseTextForAI_whenFieldIsNull() throws Exception {
        when(createJournalEntryUseCase.execute(eq(USER_ID), any(), any(), eq(true)))
                .thenReturn(buildEntry(10L, "Contenido", null));

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JournalEntryRequest("Contenido", null, null))))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturn200WithEntries_whenUserHasEntries() throws Exception {
        when(listJournalEntriesUseCase.execute(USER_ID)).thenReturn(List.of(
                buildEntry(2L, "Segunda entrada", Mood.CALM),
                buildEntry(1L, "Primera entrada", Mood.HAPPY)
        ));

        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].content").value("Segunda entrada"))
                .andExpect(jsonPath("$[0].mood").value("CALM"))
                .andExpect(jsonPath("$[1].id").value(1L));
    }

    @Test
    void list_shouldReturn200WithEmptyList_whenUserHasNoEntries() throws Exception {
        when(listJournalEntriesUseCase.execute(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void list_shouldReturn200WithNullMood_whenEntryHasNoMood() throws Exception {
        when(listJournalEntriesUseCase.execute(USER_ID)).thenReturn(List.of(
                buildEntry(1L, "Sin mood", null)
        ));

        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mood").doesNotExist());
    }
}
