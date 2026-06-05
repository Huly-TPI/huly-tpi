package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import com.huly.backend.infrastructure.presentation.controller.BotConfigController;
import com.huly.backend.infrastructure.presentation.dto.UpdateBotConfigRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
        BotConfigController controller = new BotConfigController(getBotConfigUseCase, updateBotConfigUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getConfig_shouldReturn200WithCurrentConfig() throws Exception {
        ChatConfig config = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("mi prompt").build();
        when(getBotConfigUseCase.execute()).thenReturn(config);

        mockMvc.perform(get("/api/admin/chat/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.risk_detection_enabled").value(true))
                .andExpect(jsonPath("$.system_prompt").value("mi prompt"));
    }

    @Test
    void getConfig_shouldReturn200WithNullFields_whenConfigHasNulls() throws Exception {
        ChatConfig config = ChatConfig.builder().id(null).riskDetectionEnabled(null).systemPrompt(null).build();
        when(getBotConfigUseCase.execute()).thenReturn(config);

        mockMvc.perform(get("/api/admin/chat/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.risk_detection_enabled").doesNotExist());
    }

    @Test
    void updateConfig_shouldReturn200WithUpdatedConfig_whenRequestIsValid() throws Exception {
        ChatConfig updated = ChatConfig.builder().id(1L).riskDetectionEnabled(false).systemPrompt("nuevo").build();
        when(updateBotConfigUseCase.execute(any())).thenReturn(updated);

        UpdateBotConfigRequest request = new UpdateBotConfigRequest(false, "nuevo");

        mockMvc.perform(put("/api/admin/chat/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.risk_detection_enabled").value(false))
                .andExpect(jsonPath("$.system_prompt").value("nuevo"));
    }

    @Test
    void updateConfig_shouldReturn400_whenSystemPromptIsBlank() throws Exception {
        UpdateBotConfigRequest request = new UpdateBotConfigRequest(true, "");

        mockMvc.perform(put("/api/admin/chat/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateConfig_shouldReturn400_whenSystemPromptIsNull() throws Exception {
        mockMvc.perform(put("/api/admin/chat/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"risk_detection_enabled\": true, \"systemPrompt\": null}"))
                .andExpect(status().isBadRequest());
    }
}
