package com.huly.backend.domain.useCase.chatConfig;

import com.huly.backend.domain.model.UpdateBotConfigCommand;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBotConfigUseCaseTest {

    @Mock
    private BotConfigService botConfigService;

    @InjectMocks
    private UpdateBotConfigUseCase updateBotConfigUseCase;

    @Test
    void execute_shouldDelegateToServiceAndReturnUpdatedConfig() {
        UpdateBotConfigCommand command = new UpdateBotConfigCommand(true, "nuevo prompt");
        ChatConfig updated = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("nuevo prompt").build();
        when(botConfigService.updateConfig(command)).thenReturn(updated);

        ChatConfig result = updateBotConfigUseCase.execute(command);

        assertThat(result).isEqualTo(updated);
        verify(botConfigService).updateConfig(command);
    }

    @Test
    void execute_shouldPropagateExceptionFromService() {
        UpdateBotConfigCommand command = new UpdateBotConfigCommand(false, "prompt");
        when(botConfigService.updateConfig(command)).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> updateBotConfigUseCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
