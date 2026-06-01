package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.model.JournalEntry;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.useCase.journal.CreateJournalEntryUseCase;
import com.huly.backend.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.presentation.dto.journal.JournalEntryRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JournalControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CreateJournalEntryUseCase createJournalEntryUseCase;
    private AppUserRepository appUserRepository;

    private static final String USER_EMAIL = "test@huly.com";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        createJournalEntryUseCase = mock(CreateJournalEntryUseCase.class);
        appUserRepository = mock(AppUserRepository.class);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(USER_EMAIL);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        JournalController controller = new JournalController(createJournalEntryUseCase, appUserRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AppUserEntity mockUser() {
        return AppUserEntity.builder().id(USER_ID).build();
    }

    private JournalEntry mockEntry(Mood mood) {
        return JournalEntry.builder()
                .id(10L)
                .userId(USER_ID)
                .journalId(1L)
                .content("Hoy me sentí bien")
                .mood(mood)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void create_shouldReturn201_whenRequestHasValidContentAndMood() throws Exception {
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(createJournalEntryUseCase.execute(eq(USER_ID), eq("Hoy me sentí bien"), eq(Mood.HAPPY)))
                .thenReturn(mockEntry(Mood.HAPPY));

        JournalEntryRequest request = new JournalEntryRequest("Hoy me sentí bien", "HAPPY");

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("Hoy me sentí bien"))
                .andExpect(jsonPath("$.mood").value("HAPPY"));
    }

    @Test
    void create_shouldReturn201_whenMoodIsNull() throws Exception {
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(createJournalEntryUseCase.execute(eq(USER_ID), eq("Solo escribir"), eq(null)))
                .thenReturn(mockEntry(null));

        JournalEntryRequest request = new JournalEntryRequest("Solo escribir", null);

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mood").doesNotExist());
    }

    @Test
    void create_shouldReturn201_whenMoodIsLowercase() throws Exception {
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));
        when(createJournalEntryUseCase.execute(eq(USER_ID), any(), eq(Mood.SAD)))
                .thenReturn(mockEntry(Mood.SAD));

        JournalEntryRequest request = new JournalEntryRequest("Hoy fue difícil", "sad");

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn400_whenContentIsBlank() throws Exception {
        JournalEntryRequest request = new JournalEntryRequest("", "HAPPY");

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(mockUser()));

        JournalEntryRequest request = new JournalEntryRequest("Hoy fue raro", "INVALIDO");

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404_whenUserNotFound() throws Exception {
        when(appUserRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        JournalEntryRequest request = new JournalEntryRequest("Contenido válido", null);

        mockMvc.perform(post("/api/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
