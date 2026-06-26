package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.SaveUserAntiScrollSettingsRequest;
import com.huly.backend.domain.mapper.extension.SaveUserAntiScrollSettingsMapper;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private SaveUserAntiScrollSettingsUseCase saveUserAntiScrollSettingsUseCase;

    @BeforeEach
    void setUp() {
        saveUserAntiScrollSettingsUseCase =
                new SaveUserAntiScrollSettingsUseCase(settingsRepository, new SaveUserAntiScrollSettingsMapper());
    }

    @Test
    void execute_shouldSaveSettingsInRepository() {
        SaveUserAntiScrollSettingsRequest request = new SaveUserAntiScrollSettingsRequest(
                10L, true, 15, List.of("x.com"), false);

        saveUserAntiScrollSettingsUseCase.execute(request);

        verify(settingsRepository).save(eq(10L), argThat((UserAntiScrollSettings settings) ->
                settings.isEnabled()
                        && settings.getPauseIntervalSeconds() == 15
                        && settings.getMonitoredDomains().equals(List.of("x.com"))
                        && !settings.isDataSharingConsent()));
    }
}
