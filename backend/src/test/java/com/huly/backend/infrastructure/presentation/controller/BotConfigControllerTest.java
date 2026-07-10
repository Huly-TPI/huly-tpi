package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.UpdateBotConfigRequest;
import com.huly.backend.infrastructure.presentation.mapper.chatBotConfig.BotConfigPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BotConfigControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetBotConfigUseCase getBotConfigUseCase;
    private UpdateBotConfigUseCase updateBotConfigUseCase;

    @BeforeEach
    void setUp() {
        getBotConfigUseCase = mock(GetBotConfigUseCase.class);
        updateBotConfigUseCase = mock(UpdateBotConfigUseCase.class);
        BotConfigController controller = new BotConfigController(
                getBotConfigUseCase, updateBotConfigUseCase, new BotConfigPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Devuelve 200 con la configuración actual")
    void getConfigShouldReturn200WithCurrentConfig() throws Exception {
        givenCurrentConfig(currentConfig());

        ResultActions result = performGetConfig();

        thenOkWithCurrentConfig(result);
    }

    @Test
    @DisplayName("Devuelve 200 sin los campos nulos cuando la configuración tiene nulos")
    void getConfigShouldReturn200WithNullFieldsWhenConfigHasNulls() throws Exception {
        givenCurrentConfig(configWithNulls());

        ResultActions result = performGetConfig();

        thenOkWithoutNullFields(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la configuración actualizada cuando el request es válido")
    void updateConfigShouldReturn200WithUpdatedConfigWhenRequestIsValid() throws Exception {
        givenUpdateReturns(updatedConfig());
        String body = validUpdateRequestJson();

        ResultActions result = performUpdate(body);

        thenOkWithUpdatedConfig(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el system prompt está vacío")
    void updateConfigShouldReturn400WhenSystemPromptIsBlank() throws Exception {
        String body = blankPromptRequestJson();

        ResultActions result = performUpdate(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el system prompt es nulo")
    void updateConfigShouldReturn400WhenSystemPromptIsNull() throws Exception {
        String body = nullPromptRequestJson();

        ResultActions result = performUpdate(body);

        thenBadRequest(result);
    }

    // --- arrange ---
    private void givenCurrentConfig(GetBotConfigResponse config) {
        when(getBotConfigUseCase.execute()).thenReturn(config);
    }

    private void givenUpdateReturns(UpdateBotConfigResponse response) {
        when(updateBotConfigUseCase.execute(any())).thenReturn(response);
    }

    private GetBotConfigResponse currentConfig() {
        return new GetBotConfigResponse(1L, true, "mi prompt", true, false);
    }

    private GetBotConfigResponse configWithNulls() {
        return new GetBotConfigResponse(null, null, null, null, null);
    }

    private UpdateBotConfigResponse updatedConfig() {
        return new UpdateBotConfigResponse(1L, false, "nuevo", false, true);
    }

    private String validUpdateRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new UpdateBotConfigRequest(false, "nuevo", false, true));
    }

    private String blankPromptRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new UpdateBotConfigRequest(true, ""));
    }

    private String nullPromptRequestJson() {
        return "{\"riskDetectionEnabled\": true, \"systemPrompt\": null}";
    }

    // --- act ---
    private ResultActions performGetConfig() throws Exception {
        return mockMvc.perform(get("/api/admin/chat/config"));
    }

    private ResultActions performUpdate(String body) throws Exception {
        return mockMvc.perform(put("/api/admin/chat/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // --- assert ---
    private void thenOkWithCurrentConfig(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.riskDetectionEnabled").value(true))
                .andExpect(jsonPath("$.systemPrompt").value("mi prompt"))
                .andExpect(jsonPath("$.preferredNameQuestionEnabled").value(true))
                .andExpect(jsonPath("$.communicationStyleQuestionEnabled").value(false));
    }

    private void thenOkWithoutNullFields(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.riskDetectionEnabled").doesNotExist());
    }

    private void thenOkWithUpdatedConfig(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.riskDetectionEnabled").value(false))
                .andExpect(jsonPath("$.systemPrompt").value("nuevo"))
                .andExpect(jsonPath("$.preferredNameQuestionEnabled").value(false))
                .andExpect(jsonPath("$.communicationStyleQuestionEnabled").value(true));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }
}
