package com.huly.backend.domain.service;

import com.huly.backend.domain.model.UpdateBotConfigCommand;
import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotConfigServiceTest {

    @Mock
    private ChatConfigRepository chatConfigRepository;

    @InjectMocks
    private BotConfigService botConfigService;

    // ── getConfig ────────────────────────────────────────────────────────────

    @Test
    void getConfig_shouldReturnConfigFromRepository_whenExists() {
        ChatConfig config = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("prompt").build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(config));

        ChatConfig result = botConfigService.getConfig();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void getConfig_shouldReturnDefaultConfig_whenNotExists() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());

        ChatConfig result = botConfigService.getConfig();

        assertThat(result.getId()).isNull();
        assertThat(result.getRiskDetectionEnabled()).isFalse();
        assertThat(result.getSystemPrompt()).isEmpty();
    }

    // ── updateConfig ─────────────────────────────────────────────────────────

    @Test
    void updateConfig_shouldUpdateBothFields_whenCommandHasAllValues() {
        ChatConfig existing = ChatConfig.builder().id(1L).riskDetectionEnabled(false).systemPrompt("viejo").build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(existing));
        when(chatConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBotConfigCommand command = new UpdateBotConfigCommand(true, "nuevo prompt");
        ChatConfig result = botConfigService.updateConfig(command);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRiskDetectionEnabled()).isTrue();
        assertThat(result.getSystemPrompt()).isEqualTo("nuevo prompt");
    }

    @Test
    void updateConfig_shouldKeepExistingRiskDetection_whenCommandHasNullRiskDetection() {
        ChatConfig existing = ChatConfig.builder().id(1L).riskDetectionEnabled(true).systemPrompt("prompt").build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(existing));
        when(chatConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBotConfigCommand command = new UpdateBotConfigCommand(null, "nuevo prompt");
        ChatConfig result = botConfigService.updateConfig(command);

        assertThat(result.getRiskDetectionEnabled()).isTrue();
        assertThat(result.getSystemPrompt()).isEqualTo("nuevo prompt");
    }

    @Test
    void updateConfig_shouldKeepExistingSystemPrompt_whenCommandHasNullSystemPrompt() {
        ChatConfig existing = ChatConfig.builder().id(1L).riskDetectionEnabled(false).systemPrompt("prompt original").build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(existing));
        when(chatConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBotConfigCommand command = new UpdateBotConfigCommand(true, null);
        ChatConfig result = botConfigService.updateConfig(command);

        assertThat(result.getRiskDetectionEnabled()).isTrue();
        assertThat(result.getSystemPrompt()).isEqualTo("prompt original");
    }

    @Test
    void updateConfig_shouldPersistUpdatedConfig() {
        ChatConfig existing = ChatConfig.builder().id(2L).riskDetectionEnabled(false).systemPrompt("old").build();
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(existing));
        ChatConfig saved = ChatConfig.builder().id(2L).riskDetectionEnabled(true).systemPrompt("new").build();
        when(chatConfigRepository.save(any())).thenReturn(saved);

        ChatConfig result = botConfigService.updateConfig(new UpdateBotConfigCommand(true, "new"));

        ArgumentCaptor<ChatConfig> captor = ArgumentCaptor.forClass(ChatConfig.class);
        verify(chatConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(2L);
        assertThat(result).isEqualTo(saved);
    }
}
