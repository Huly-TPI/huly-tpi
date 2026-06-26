package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import com.huly.backend.infrastructure.presentation.dto.chatConfig.UpdateBotConfigRequest;
import com.huly.backend.infrastructure.presentation.mapper.chatBotConfig.BotConfigPresentationMapper;
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
        BotConfigController controller = new BotConfigController(
                getBotConfigUseCase, updateBotConfigUseCase, new BotConfigPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getConfig_shouldReturn200WithCurrentConfig() throws Exception {
        GetBotConfigResponse config = new GetBotConfigResponse(1L, true, "mi prompt", true, false);
        when(getBotConfigUseCase.execute()).thenReturn(config);

        mockMvc.perform(get("/api/admin/chat/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.risk_detection_enabled").value(true))
                .andExpect(jsonPath("$.system_prompt").value("mi prompt"))
                .andExpect(jsonPath("$.preferred_name_question_enabled").value(true))
                .andExpect(jsonPath("$.communication_style_question_enabled").value(false));
    }

    @Test
    void getConfig_shouldReturn200WithNullFields_whenConfigHasNulls() throws Exception {
        GetBotConfigResponse config = new GetBotConfigResponse(null, null, null, null, null);
        when(getBotConfigUseCase.execute()).thenReturn(config);

        mockMvc.perform(get("/api/admin/chat/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.risk_detection_enabled").doesNotExist());
    }

    @Test
    void updateConfig_shouldReturn200WithUpdatedConfig_whenRequestIsValid() throws Exception {
        UpdateBotConfigResponse updated = new UpdateBotConfigResponse(1L, false, "nuevo", false, true);
        when(updateBotConfigUseCase.execute(any())).thenReturn(updated);

        UpdateBotConfigRequest request = new UpdateBotConfigRequest(false, "nuevo", false, true);

        mockMvc.perform(put("/api/admin/chat/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.risk_detection_enabled").value(false))
                .andExpect(jsonPath("$.system_prompt").value("nuevo"))
                .andExpect(jsonPath("$.preferred_name_question_enabled").value(false))
                .andExpect(jsonPath("$.communication_style_question_enabled").value(true));
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
