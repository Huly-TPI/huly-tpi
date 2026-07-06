package com.huly.backend.domain.service;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.model.chat.UpdateBotConfigCommand;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.domain.service.chat.BotConfigService;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("Devuelve la configuración almacenada cuando existe")
    void getConfigShouldReturnStoredConfigWhenExists() {
        ChatConfig stored = storedConfig(1L, true, "prompt");
        givenStoredConfig(stored);

        ChatConfig result = getConfig();

        thenResultIs(result, stored);
    }

    @Test
    @DisplayName("Devuelve la configuración por defecto cuando no existe")
    void getConfigShouldReturnDefaultConfigWhenNotExists() {
        givenNoStoredConfig();

        ChatConfig result = getConfig();

        thenDefaultConfig(result);
    }

    @Test
    @DisplayName("Actualiza riesgo y prompt cuando el comando trae ambos valores")
    void updateConfigShouldUpdateBothFieldsWhenCommandHasAllValues() {
        givenStoredConfig(storedConfig(1L, false, "viejo"));
        givenSaveReturnsArgument();

        ChatConfig result = updateConfig(command(true, "nuevo prompt"));

        thenIdIs(result, 1L);
        thenRiskAndPrompt(result, true, "nuevo prompt");
    }

    @Test
    @DisplayName("Conserva la detección de riesgo existente cuando el comando la trae en null")
    void updateConfigShouldKeepExistingRiskDetectionWhenCommandHasNull() {
        givenStoredConfig(storedConfig(1L, true, "prompt"));
        givenSaveReturnsArgument();

        ChatConfig result = updateConfig(command(null, "nuevo prompt"));

        thenRiskAndPrompt(result, true, "nuevo prompt");
    }

    @Test
    @DisplayName("Conserva el prompt existente cuando el comando lo trae en null")
    void updateConfigShouldKeepExistingSystemPromptWhenCommandHasNull() {
        givenStoredConfig(storedConfig(1L, false, "prompt original"));
        givenSaveReturnsArgument();

        ChatConfig result = updateConfig(command(true, null));

        thenRiskAndPrompt(result, true, "prompt original");
    }

    @Test
    @DisplayName("Persiste la configuración actualizada y devuelve la guardada")
    void updateConfigShouldPersistUpdatedConfig() {
        givenStoredConfig(storedConfig(2L, false, "old"));
        ChatConfig saved = storedConfig(2L, true, "new");
        givenSaveReturns(saved);

        ChatConfig result = updateConfig(command(true, "new"));

        thenPersistedConfigId(2L);
        thenResultIs(result, saved);
    }

    @Test
    @DisplayName("Actualiza los flags de personalización cuando el comando los trae")
    void updateConfigShouldUpdatePersonalizationFlagsWhenCommandHasThem() {
        givenStoredConfig(fullConfig(1L, true, "prompt", true, true));
        givenSaveReturnsArgument();

        ChatConfig result = updateConfig(command(true, "prompt", false, false));

        thenPersonalizationFlags(result, false, false);
    }

    @Test
    @DisplayName("Conserva los flags de personalización existentes cuando el comando los omite")
    void updateConfigShouldKeepExistingPersonalizationFlagsWhenCommandOmitsThem() {
        givenStoredConfig(fullConfig(1L, true, "prompt", true, true));
        givenSaveReturnsArgument();

        ChatConfig result = updateConfig(command(true, "prompt"));

        thenPersonalizationFlags(result, true, true);
    }

    // --- arrange ---
    private void givenStoredConfig(ChatConfig config) {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.of(config));
    }

    private void givenNoStoredConfig() {
        when(chatConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    private void givenSaveReturnsArgument() {
        when(chatConfigRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenSaveReturns(ChatConfig saved) {
        when(chatConfigRepository.save(any())).thenReturn(saved);
    }

    private ChatConfig storedConfig(Long id, boolean riskDetectionEnabled, String systemPrompt) {
        return ChatConfig.builder()
                .id(id)
                .riskDetectionEnabled(riskDetectionEnabled)
                .systemPrompt(systemPrompt)
                .build();
    }

    private ChatConfig fullConfig(
            Long id,
            boolean riskDetectionEnabled,
            String systemPrompt,
            boolean preferredNameQuestionEnabled,
            boolean communicationStyleQuestionEnabled) {
        return new ChatConfig(
                id, riskDetectionEnabled, systemPrompt, preferredNameQuestionEnabled, communicationStyleQuestionEnabled);
    }

    private UpdateBotConfigCommand command(Boolean riskDetectionEnabled, String systemPrompt) {
        return new UpdateBotConfigCommand(riskDetectionEnabled, systemPrompt);
    }

    private UpdateBotConfigCommand command(
            Boolean riskDetectionEnabled,
            String systemPrompt,
            Boolean preferredNameQuestionEnabled,
            Boolean communicationStyleQuestionEnabled) {
        return new UpdateBotConfigCommand(
                riskDetectionEnabled, systemPrompt, preferredNameQuestionEnabled, communicationStyleQuestionEnabled);
    }

    // --- act ---
    private ChatConfig getConfig() {
        return botConfigService.getConfig();
    }

    private ChatConfig updateConfig(UpdateBotConfigCommand command) {
        return botConfigService.updateConfig(command);
    }

    // --- assert ---
    private void thenResultIs(ChatConfig result, ChatConfig expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenDefaultConfig(ChatConfig result) {
        assertThat(result.getId()).isNull();
        assertThat(result.getRiskDetectionEnabled()).isFalse();
        assertThat(result.getSystemPrompt()).isEmpty();
        assertThat(result.getPreferredNameQuestionEnabled()).isTrue();
        assertThat(result.getCommunicationStyleQuestionEnabled()).isTrue();
    }

    private void thenIdIs(ChatConfig result, Long id) {
        assertThat(result.getId()).isEqualTo(id);
    }

    private void thenRiskAndPrompt(ChatConfig result, boolean riskDetectionEnabled, String systemPrompt) {
        assertThat(result.getRiskDetectionEnabled()).isEqualTo(riskDetectionEnabled);
        assertThat(result.getSystemPrompt()).isEqualTo(systemPrompt);
    }

    private void thenPersonalizationFlags(
            ChatConfig result, boolean preferredNameQuestionEnabled, boolean communicationStyleQuestionEnabled) {
        assertThat(result.getPreferredNameQuestionEnabled()).isEqualTo(preferredNameQuestionEnabled);
        assertThat(result.getCommunicationStyleQuestionEnabled()).isEqualTo(communicationStyleQuestionEnabled);
    }

    private void thenPersistedConfigId(Long id) {
        ArgumentCaptor<ChatConfig> captor = ArgumentCaptor.forClass(ChatConfig.class);
        verify(chatConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
    }
}
