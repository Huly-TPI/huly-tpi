package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.dto.journal.CreateJournalEntryResponse;
import com.huly.backend.domain.dto.journal.JournalEntryItem;
import com.huly.backend.domain.dto.journal.ListJournalEntriesResponse;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.domain.useCase.journal.ListJournalEntriesUseCase;
import com.huly.backend.infrastructure.presentation.dto.journal.JournalEntryRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.journal.JournalPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JournalControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CreateJournalEntryUseCase createJournalEntryUseCase;
    private ListJournalEntriesUseCase listJournalEntriesUseCase;

    @BeforeEach
    void setUp() {
        createJournalEntryUseCase = mock(CreateJournalEntryUseCase.class);
        listJournalEntriesUseCase = mock(ListJournalEntriesUseCase.class);

        JournalController controller = new JournalController(
                createJournalEntryUseCase, listJournalEntriesUseCase, new JournalPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── POST /api/journal ────────────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 201 cuando el pedido tiene contenido y mood válidos")
    void createShouldReturn201WhenRequestHasValidContentAndMood() throws Exception {
        // --- arrange ---
        givenCreateReturnsForContentAndMood("Hoy me sentí bien", Mood.HAPPY);
        // --- act ---
        ResultActions result = performCreate("Hoy me sentí bien", "HAPPY", true);
        // --- assert ---
        thenCreatedWithEntry(result, 10L, "Hoy me sentí bien", "HAPPY");
    }

    @Test
    @DisplayName("Devuelve 201 cuando el mood es null")
    void createShouldReturn201WhenMoodIsNull() throws Exception {
        // --- arrange ---
        givenCreateReturnsForContentAndMood("Solo escribir", null);
        // --- act ---
        ResultActions result = performCreate("Solo escribir", null, null);
        // --- assert ---
        thenCreatedWithoutMood(result);
    }

    @Test
    @DisplayName("Devuelve 201 cuando el mood viene en minúsculas")
    void createShouldReturn201WhenMoodIsLowercase() throws Exception {
        // --- arrange ---
        givenCreateReturnsForContentAndMood("Hoy fue difícil", Mood.SAD);
        // --- act ---
        ResultActions result = performCreate("Hoy fue difícil", "sad", true);
        // --- assert ---
        thenCreated(result);
    }

    @Test
    @DisplayName("Devuelve 201 cuando el mood viene en blanco y se guarda como null")
    void createShouldReturn201WhenMoodIsBlank() throws Exception {
        // --- arrange ---
        givenCreateReturnsForContentAndMood("Contenido", null);
        // --- act ---
        ResultActions result = performCreate("Contenido", "   ", null);
        // --- assert ---
        thenCreatedWithoutMood(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el contenido está en blanco")
    void createShouldReturn400WhenContentIsBlank() throws Exception {
        // --- act ---
        ResultActions result = performCreate("", "HAPPY", null);
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el contenido es null")
    void createShouldReturn400WhenContentIsNull() throws Exception {
        // --- act ---
        ResultActions result = performCreateWithRawBody("{\"mood\":\"HAPPY\"}");
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el mood no es un valor válido")
    void createShouldReturn400WhenMoodIsInvalid() throws Exception {
        // --- act ---
        ResultActions result = performCreate("Hoy fue raro", "INVALIDO", null);
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Pasa useTextForAI en true al caso de uso cuando el campo es true")
    void createShouldPassUseTextForAITrueToUseCaseWhenFieldIsTrue() throws Exception {
        // --- arrange ---
        givenCreateReturnsForUseTextForAI(true);
        // --- act ---
        ResultActions result = performCreate("Contenido", "HAPPY", true);
        // --- assert ---
        thenCreated(result);
    }

    @Test
    @DisplayName("Pasa useTextForAI en false al caso de uso cuando el campo es false")
    void createShouldPassUseTextForAIFalseToUseCaseWhenFieldIsFalse() throws Exception {
        // --- arrange ---
        givenCreateReturnsForUseTextForAI(false);
        // --- act ---
        ResultActions result = performCreate("Contenido", "HAPPY", false);
        // --- assert ---
        thenCreated(result);
    }

    @Test
    @DisplayName("Usa useTextForAI en true por defecto cuando el campo es null")
    void createShouldDefaultToTrueForUseTextForAIWhenFieldIsNull() throws Exception {
        // --- arrange ---
        givenCreateReturnsForUseTextForAI(true);
        // --- act ---
        ResultActions result = performCreate("Contenido", null, null);
        // --- assert ---
        thenCreated(result);
    }

    @Test
    @DisplayName("Devuelve 401 al crear cuando no está autenticado")
    void createShouldReturn401WhenNotAuthenticated() throws Exception {
        // --- arrange ---
        givenNoAuthentication();
        // --- act ---
        ResultActions result = performCreate("Contenido", "HAPPY", true);
        // --- assert ---
        thenUnauthorized(result);
    }

    // ── GET /api/journal ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 200 con las entradas cuando el usuario tiene entradas")
    void listShouldReturn200WithEntriesWhenUserHasEntries() throws Exception {
        // --- arrange ---
        givenEntries(List.of(
                buildItem(2L, "Segunda entrada", Mood.CALM),
                buildItem(1L, "Primera entrada", Mood.HAPPY)));
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenOkWithEntries(result);
    }

    @Test
    @DisplayName("Devuelve 200 con una lista vacía cuando el usuario no tiene entradas")
    void listShouldReturn200WithEmptyListWhenUserHasNoEntries() throws Exception {
        // --- arrange ---
        givenEntries(List.of());
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenOkWithEmptyList(result);
    }

    @Test
    @DisplayName("Devuelve 200 con mood null cuando la entrada no tiene mood")
    void listShouldReturn200WithNullMoodWhenEntryHasNoMood() throws Exception {
        // --- arrange ---
        givenEntries(List.of(buildItem(1L, "Sin mood", null)));
        // --- act ---
        ResultActions result = performList();
        // --- assert ---
        thenOkWithEntryWithoutMood(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenCreateReturnsForContentAndMood(String content, Mood mood) {
        when(createJournalEntryUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && content.equals(r.content()) && r.mood() == mood)))
                .thenReturn(buildResponse(10L, content, mood));
    }

    private void givenCreateReturnsForUseTextForAI(boolean useTextForAI) {
        when(createJournalEntryUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && r.useTextForAI() == useTextForAI)))
                .thenReturn(buildResponse(10L, "Contenido", Mood.HAPPY));
    }

    private void givenEntries(List<JournalEntryItem> items) {
        when(listJournalEntriesUseCase.execute(argThat(r -> r != null && USER_ID.equals(r.userId()))))
                .thenReturn(new ListJournalEntriesResponse(items));
    }

    private CreateJournalEntryResponse buildResponse(Long id, String content, Mood mood) {
        return new CreateJournalEntryResponse(id, content, mood, Instant.now());
    }

    private JournalEntryItem buildItem(Long id, String content, Mood mood) {
        return new JournalEntryItem(id, content, mood, Instant.now());
    }

    // --- act ---
    private ResultActions performCreate(String content, String mood, Boolean useTextForAI) throws Exception {
        return mockMvc.perform(post("/api/journal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new JournalEntryRequest(content, mood, useTextForAI))));
    }

    private ResultActions performCreateWithRawBody(String json) throws Exception {
        return mockMvc.perform(post("/api/journal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions performList() throws Exception {
        return mockMvc.perform(get("/api/journal"));
    }

    // --- assert ---
    private void thenCreatedWithEntry(ResultActions result, long id, String content, String mood) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.mood").value(mood));
    }

    private void thenCreatedWithoutMood(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.mood").doesNotExist());
    }

    private void thenCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenOkWithEntries(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].content").value("Segunda entrada"))
                .andExpect(jsonPath("$[0].mood").value("CALM"))
                .andExpect(jsonPath("$[1].id").value(1L));
    }

    private void thenOkWithEmptyList(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void thenOkWithEntryWithoutMood(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mood").doesNotExist());
    }
}
