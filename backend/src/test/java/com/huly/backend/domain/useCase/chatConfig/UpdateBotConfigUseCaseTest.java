package com.huly.backend.domain.useCase.chatConfig;

import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigRequest;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.mapper.chatBotConfig.UpdateBotConfigMapper;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.chat.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBotConfigUseCaseTest {

    @Mock
    private BotConfigService botConfigService;

    private UpdateBotConfigUseCase updateBotConfigUseCase;

    @BeforeEach
    void setUp() {
        updateBotConfigUseCase = new UpdateBotConfigUseCase(botConfigService, new UpdateBotConfigMapper());
    }

    @Test
    void execute_shouldDelegateToServiceAndReturnUpdatedConfig() {
        UpdateBotConfigRequest request = new UpdateBotConfigRequest(true, "nuevo prompt", null, null);
        ChatConfig updated = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("nuevo prompt").build();
        when(botConfigService.updateConfig(argThat(cmd ->
                Boolean.TRUE.equals(cmd.riskDetectionEnabled()) && "nuevo prompt".equals(cmd.systemPrompt()))))
                .thenReturn(updated);

        UpdateBotConfigResponse result = updateBotConfigUseCase.execute(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.riskDetectionEnabled()).isTrue();
        assertThat(result.systemPrompt()).isEqualTo("nuevo prompt");
        verify(botConfigService).updateConfig(argThat(cmd ->
                Boolean.TRUE.equals(cmd.riskDetectionEnabled()) && "nuevo prompt".equals(cmd.systemPrompt())));
    }

    @Test
    void execute_shouldPropagateExceptionFromService() {
        UpdateBotConfigRequest request = new UpdateBotConfigRequest(false, "prompt", null, null);
        when(botConfigService.updateConfig(argThat(cmd -> "prompt".equals(cmd.systemPrompt()))))
                .thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> updateBotConfigUseCase.execute(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
