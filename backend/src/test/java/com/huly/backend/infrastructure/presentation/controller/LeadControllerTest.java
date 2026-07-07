package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.lead.RegisterLeadRequest;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.useCase.lead.RegisterLeadUseCase;
import com.huly.backend.infrastructure.presentation.dto.lead.LeadRequestDto;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.lead.LeadPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
    @DisplayName("Devuelve 201 cuando la solicitud de registro es válida")
    void registerShouldReturn201WhenRequestIsValid() throws Exception {
        String body = givenValidLeadBody();

        ResultActions result = performRegister(body);

        thenCreatedWithSuccessMessage(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el email es inválido")
    void registerShouldReturn400WhenEmailIsInvalid() throws Exception {
        String body = givenInvalidEmailLeadBody();

        ResultActions result = performRegister(body);

        thenBadRequestAndNotRegistered(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el nickname es demasiado corto")
    void registerShouldReturn400WhenNicknameIsTooShort() throws Exception {
        String body = givenTooShortNicknameLeadBody();

        ResultActions result = performRegister(body);

        thenBadRequestAndNotRegistered(result);
    }

    // --- arrange ---
    private String givenValidLeadBody() throws Exception {
        return leadBody("lead@huly.com", "hulyuser", SourceAction.LANDING);
    }

    private String givenInvalidEmailLeadBody() throws Exception {
        return leadBody("not-an-email", "hulyuser", SourceAction.LANDING);
    }

    private String givenTooShortNicknameLeadBody() throws Exception {
        return leadBody("lead@huly.com", "ab", SourceAction.LANDING);
    }

    private String leadBody(String email, String nickname, SourceAction source) throws Exception {
        return objectMapper.writeValueAsString(new LeadRequestDto(email, nickname, source));
    }

    // --- act ---
    private ResultActions performRegister(String body) throws Exception {
        return mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // --- assert ---
    private void thenCreatedWithSuccessMessage(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registro exitoso"));
        verify(registerLeadUseCase).execute(new RegisterLeadRequest("lead@huly.com", "hulyuser", SourceAction.LANDING));
    }

    private void thenBadRequestAndNotRegistered(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
        verify(registerLeadUseCase, never()).execute(any());
    }
}
