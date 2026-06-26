package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadRequestDto;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.lead.LeadPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeadControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RegisterLeadUseCase registerLeadUseCase;

    @BeforeEach
    void setUp() {
        registerLeadUseCase = mock(RegisterLeadUseCase.class);
        LeadController controller = new LeadController(registerLeadUseCase, new LeadPresentationMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        LeadRequestDto req = new LeadRequestDto("lead@huly.com", "hulyuser", SourceAction.LANDING);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registro exitoso"));

        verify(registerLeadUseCase).execute(new RegisterLeadRequest("lead@huly.com", "hulyuser", SourceAction.LANDING));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        LeadRequestDto req = new LeadRequestDto("not-an-email", "hulyuser", SourceAction.LANDING);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(registerLeadUseCase, never()).execute(any());
    }

    @Test
    void register_shouldReturn400_whenNicknameIsTooShort() throws Exception {
        LeadRequestDto req = new LeadRequestDto("lead@huly.com", "ab", SourceAction.LANDING);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(registerLeadUseCase, never()).execute(any());
    }
}
