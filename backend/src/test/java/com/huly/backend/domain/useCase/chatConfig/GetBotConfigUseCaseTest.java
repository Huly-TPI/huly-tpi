package com.huly.backend.domain.useCase.chatConfig;

import com.huly.backend.domain.dto.chatBotConfig.GetBotConfigResponse;
import com.huly.backend.domain.mapper.chatBotConfig.GetBotConfigMapper;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.service.chat.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.GetBotConfigUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBotConfigUseCaseTest {

    private static final Long CONFIG_ID = 1L;
    private static final String SYSTEM_PROMPT = "prompt";

    @Mock
    private BotConfigService botConfigService;

    private GetBotConfigUseCase getBotConfigUseCase;

    @BeforeEach
    void setUp() {
        getBotConfigUseCase = new GetBotConfigUseCase(botConfigService, new GetBotConfigMapper());
    }

    @Test
    @DisplayName("Devuelve la configuración mapeada cuando el servicio la provee")
    void executeShouldReturnMappedConfigWhenServiceReturnsConfig() {
        givenServiceReturnsConfig();

        GetBotConfigResponse result = getConfig();

        thenResponseMatchesConfig(result);
        thenServiceWasQueried();
    }

    @Test
    @DisplayName("Propaga la excepción cuando el servicio falla")
    void executeShouldPropagateExceptionWhenServiceFails() {
        givenServiceFails();

        thenGetConfigThrowsRuntimeException();
    }

    // --- arrange ---

    private void givenServiceReturnsConfig() {
        when(botConfigService.getConfig()).thenReturn(ChatConfig.builder()
                .id(CONFIG_ID)
                .riskDetectionEnabled(true)
                .systemPrompt(SYSTEM_PROMPT)
                .preferredNameQuestionEnabled(false)
                .communicationStyleQuestionEnabled(true)
                .build());
    }

    private void givenServiceFails() {
        when(botConfigService.getConfig()).thenThrow(new RuntimeException("error"));
    }

    // --- act ---

    private GetBotConfigResponse getConfig() {
        return getBotConfigUseCase.execute();
    }

    // --- assert ---

    /** Verifica que la respuesta refleje los cinco campos mapeados desde la configuración del servicio. */
    private void thenResponseMatchesConfig(GetBotConfigResponse result) {
        assertThat(result.id()).isEqualTo(CONFIG_ID);
        assertThat(result.riskDetectionEnabled()).isTrue();
        assertThat(result.systemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(result.preferredNameQuestionEnabled()).isFalse();
        assertThat(result.communicationStyleQuestionEnabled()).isTrue();
    }

    private void thenServiceWasQueried() {
        verify(botConfigService).getConfig();
    }

    private void thenGetConfigThrowsRuntimeException() {
        assertThatThrownBy(this::getConfig)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
