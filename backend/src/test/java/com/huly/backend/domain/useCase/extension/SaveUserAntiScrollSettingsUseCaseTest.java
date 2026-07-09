package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.domain.mapper.extension.SaveUserAntiScrollSettingsMapper;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveUserAntiScrollSettingsUseCaseTest {

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    private SaveUserAntiScrollSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveUserAntiScrollSettingsUseCase(settingsRepository, new SaveUserAntiScrollSettingsMapper());
    }

    @Test
    @DisplayName("Guarda las opciones mapeadas en el repositorio")
    void executeSavesSettingsInRepository() {
        save(10L, true, 15, List.of("x.com"), false);

        thenSettingsSaved(10L, true, 15, List.of("x.com"), false);
    }

    // --- act ---

    private void save(long userId, boolean enabled, int pauseIntervalSeconds,
                      List<String> monitoredDomains, boolean dataSharingConsent) {
        useCase.execute(new SaveUserAntiScrollSettingsRequest(
                userId, enabled, pauseIntervalSeconds, monitoredDomains, dataSharingConsent));
    }

    // --- assert ---

    private void thenSettingsSaved(long userId, boolean enabled, int pauseIntervalSeconds,
                                   List<String> monitoredDomains, boolean dataSharingConsent) {
        verify(settingsRepository).save(eq(userId), argThat((UserAntiScrollSettings settings) ->
                settings.isEnabled() == enabled
                        && settings.getPauseIntervalSeconds() == pauseIntervalSeconds
                        && settings.getMonitoredDomains().equals(monitoredDomains)
                        && settings.isDataSharingConsent() == dataSharingConsent));
    }
}
