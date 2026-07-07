package com.huly.backend.domain.useCase.chatConfig;

import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigRequest;
import com.huly.backend.domain.dto.chatBotConfig.UpdateBotConfigResponse;
import com.huly.backend.domain.mapper.chatBotConfig.UpdateBotConfigMapper;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.UpdateBotConfigCommand;
import com.huly.backend.domain.service.chat.BotConfigService;
import com.huly.backend.domain.useCase.chatBotConfig.UpdateBotConfigUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBotConfigUseCaseTest {

    private static final Long CONFIG_ID = 1L;
    private static final String SYSTEM_PROMPT = "nuevo prompt";

    @Mock
    private BotConfigService botConfigService;

    private UpdateBotConfigUseCase updateBotConfigUseCase;

    @BeforeEach
    void setUp() {
        updateBotConfigUseCase = new UpdateBotConfigUseCase(botConfigService, new UpdateBotConfigMapper());
    }

    @Test
    @DisplayName("Actualiza y devuelve la configuración mapeada cuando el servicio responde")
    void executeShouldReturnUpdatedConfigWhenServiceSucceeds() {
        givenServiceUpdatesConfig();

        UpdateBotConfigResponse result = updateConfig();

        thenResponseMatchesUpdatedConfig(result);
        thenServiceReceivedMappedCommand();
    }

    @Test
    @DisplayName("Propaga la excepción cuando el servicio falla al actualizar")
    void executeShouldPropagateExceptionWhenServiceFails() {
        givenServiceFailsOnUpdate();

        thenUpdateThrowsRuntimeException();
    }

    // --- arrange ---

    private void givenServiceUpdatesConfig() {
        when(botConfigService.updateConfig(any())).thenReturn(ChatConfig.builder()
                .id(CONFIG_ID)
                .riskDetectionEnabled(true)
                .systemPrompt(SYSTEM_PROMPT)
                .preferredNameQuestionEnabled(false)
                .communicationStyleQuestionEnabled(true)
                .build());
    }

    private void givenServiceFailsOnUpdate() {
        when(botConfigService.updateConfig(any())).thenThrow(new RuntimeException("error"));
    }

    // --- act ---

    private UpdateBotConfigResponse updateConfig() {
        return updateBotConfigUseCase.execute(
                new UpdateBotConfigRequest(true, SYSTEM_PROMPT, false, true));
    }

    // --- assert ---

    /** Verifica que la respuesta refleje los cinco campos de la configuración actualizada. */
    private void thenResponseMatchesUpdatedConfig(UpdateBotConfigResponse result) {
        assertThat(result.id()).isEqualTo(CONFIG_ID);
        assertThat(result.riskDetectionEnabled()).isTrue();
        assertThat(result.systemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(result.preferredNameQuestionEnabled()).isFalse();
        assertThat(result.communicationStyleQuestionEnabled()).isTrue();
    }

    /** Verifica que el request se mapeó a un comando con los cuatro campos correctos antes de delegar. */
    private void thenServiceReceivedMappedCommand() {
        ArgumentCaptor<UpdateBotConfigCommand> captor = ArgumentCaptor.forClass(UpdateBotConfigCommand.class);
        verify(botConfigService).updateConfig(captor.capture());
        UpdateBotConfigCommand command = captor.getValue();
        assertThat(command.riskDetectionEnabled()).isTrue();
        assertThat(command.systemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(command.preferredNameQuestionEnabled()).isFalse();
        assertThat(command.communicationStyleQuestionEnabled()).isTrue();
    }

    private void thenUpdateThrowsRuntimeException() {
        assertThatThrownBy(this::updateConfig)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
