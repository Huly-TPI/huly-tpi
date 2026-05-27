package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.BotConfigService;
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
class GetBotConfigUseCaseTest {

    @Mock
    private BotConfigService botConfigService;

    @InjectMocks
    private GetBotConfigUseCase getBotConfigUseCase;

    @Test
    void execute_shouldDelegateToServiceAndReturnConfig() {
        ChatConfig config = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("prompt").build();
        when(botConfigService.getConfig()).thenReturn(config);

        ChatConfig result = getBotConfigUseCase.execute();

        assertThat(result).isEqualTo(config);
        verify(botConfigService).getConfig();
    }

    @Test
    void execute_shouldPropagateExceptionFromService() {
        when(botConfigService.getConfig()).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> getBotConfigUseCase.execute())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
