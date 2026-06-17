package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveUserAntiScrollSettingsUseCaseTest {

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @InjectMocks
    private SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase;

    @Test
    void execute_shouldSaveSettingsInRepository() {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(15)
                .monitoredDomains(List.of("x.com"))
                .dataSharingConsent(false)
                .build();

        saveUserAntiScrollSettingsUseCase.execute(10L, settings);

        verify(settingsRepository).save(10L, settings);
    }
}
