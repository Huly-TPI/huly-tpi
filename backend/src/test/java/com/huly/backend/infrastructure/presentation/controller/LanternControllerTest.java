package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.LanternThought;
import com.huly.backend.domain.model.enums.LanternStatus;
import com.huly.backend.domain.useCase.lantern.CreateLanternThoughtUseCase;
import com.huly.backend.domain.useCase.lantern.ListLanternThoughtsUseCase;
import com.huly.backend.domain.useCase.lantern.MarkWorkedOnUseCase;
import com.huly.backend.domain.useCase.lantern.UpdateLanternStatusUseCase;
import com.huly.backend.infrastructure.presentation.controller.LanternController;
import com.huly.backend.infrastructure.presentation.dto.lantern.LanternThoughtRequest;
import com.huly.backend.infrastructure.presentation.dto.lantern.UpdateLanternStatusRequest;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LanternControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateLanternThoughtUseCase createUseCase;
    private ListLanternThoughtsUseCase listUseCase;
    private UpdateLanternStatusUseCase updateStatusUseCase;
    private MarkWorkedOnUseCase markWorkedOnUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateLanternThoughtUseCase.class);
        listUseCase = mock(ListLanternThoughtsUseCase.class);
        updateStatusUseCase = mock(UpdateLanternStatusUseCase.class);
        markWorkedOnUseCase = mock(MarkWorkedOnUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        LanternController controller = new LanternController(
                createUseCase, listUseCase, updateStatusUseCase, markWorkedOnUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void list_shouldReturn200WithLanternList() throws Exception {
        LanternThought thought = LanternThought.builder()
                .id(1L).userId(USER_ID).text("pensamiento").status(LanternStatus.ACTIVE)
                .workedOn(false).createdAt(Instant.parse("2025-01-01T00:00:00Z")).build();
        when(listUseCase.execute(USER_ID)).thenReturn(List.of(thought));

        mockMvc.perform(get("/api/lanterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("pensamiento"))
                .andExpect(jsonPath("$[0].workedOn").value(false));
    }

    @Test
    void list_shouldReturn200WithEmptyList_whenNoLanterns() throws Exception {
        when(listUseCase.execute(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/lanterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_shouldReturn201WithCreatedLantern() throws Exception {
        LanternThought thought = LanternThought.builder()
                .id(2L).userId(USER_ID).text("nuevo pensamiento").status(LanternStatus.ACTIVE)
                .workedOn(false).createdAt(Instant.parse("2025-01-01T00:00:00Z")).build();
        when(createUseCase.execute(USER_ID, "nuevo pensamiento")).thenReturn(thought);

        mockMvc.perform(post("/api/lanterns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LanternThoughtRequest("nuevo pensamiento"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.text").value("nuevo pensamiento"))
                .andExpect(jsonPath("$.workedOn").value(false));
    }

    @Test
    void create_shouldReturn400_whenTextIsBlank() throws Exception {
        mockMvc.perform(post("/api/lanterns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LanternThoughtRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn204_whenStatusIsCompleted() throws Exception {
        LanternThought updated = LanternThought.builder().id(1L).status(LanternStatus.COMPLETED).build();
        when(updateStatusUseCase.execute(eq(1L), eq(USER_ID), eq(LanternStatus.COMPLETED))).thenReturn(updated);

        mockMvc.perform(patch("/api/lanterns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLanternStatusRequest("COMPLETED"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn204_whenStatusIsCancelled() throws Exception {
        LanternThought updated = LanternThought.builder().id(1L).status(LanternStatus.CANCELLED).build();
        when(updateStatusUseCase.execute(eq(1L), eq(USER_ID), eq(LanternStatus.CANCELLED))).thenReturn(updated);

        mockMvc.perform(patch("/api/lanterns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLanternStatusRequest("CANCELLED"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusStringIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/lanterns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLanternStatusRequest("ESTADO_INVALIDO"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_shouldReturn400_whenThoughtIsNotActive() throws Exception {
        when(updateStatusUseCase.execute(eq(1L), eq(USER_ID), eq(LanternStatus.COMPLETED)))
                .thenThrow(new IllegalStateException("Solo se puede cambiar el estado de un farolito activo"));

        mockMvc.perform(patch("/api/lanterns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLanternStatusRequest("COMPLETED"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markWorkedOn_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/api/lanterns/1/worked-on"))
                .andExpect(status().isNoContent());

        verify(markWorkedOnUseCase).execute(1L, USER_ID);
    }
}
