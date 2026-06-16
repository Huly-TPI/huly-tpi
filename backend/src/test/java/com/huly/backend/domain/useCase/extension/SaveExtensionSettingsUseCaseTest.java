package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveExtensionSettingsUseCaseTest {

    @Mock
    private ExtensionSettingsRepository settingsRepository;

    @InjectMocks
    private SaveExtensionSettingsUseCase saveExtensionSettingsUseCase;

    @Test
    void execute_shouldSaveSettingsInRepository() {
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .pauseIntervalMinutes(15)
                .gardenUrl("http://localhost:5173/garden")
                .backendUrl("http://localhost:8080")
                .monitoredDomains(List.of("x.com"))
                .dataSharingConsent(false)
                .build();

        saveExtensionSettingsUseCase.execute(10L, settings);

        verify(settingsRepository).save(10L, settings);
    }
}
