package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExtensionSettingsUseCaseTest {

    @Mock
    private ExtensionSettingsRepository settingsRepository;

    @InjectMocks
    private GetExtensionSettingsUseCase getExtensionSettingsUseCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(getExtensionSettingsUseCase, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(getExtensionSettingsUseCase, "backendUrl", "http://localhost:8080");
    }

    @Test
    void execute_shouldReturnSettingsFromRepository_whenSettingsExist() {
        ExtensionSettings existingSettings = ExtensionSettings.builder()
                .enabled(false)
                .pauseIntervalMinutes(30)
                .gardenUrl("http://localhost:5173/garden")
                .backendUrl("http://localhost:8080")
                .monitoredDomains(List.of("twitter.com"))
                .dataSharingConsent(true)
                .build();

        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(existingSettings));

        ExtensionSettings result = getExtensionSettingsUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getPauseIntervalMinutes()).isEqualTo(30);
        assertThat(result.isDataSharingConsent()).isTrue();
        assertThat(result.getMonitoredDomains()).containsExactly("twitter.com");
    }

    @Test
    void execute_shouldReturnDefaultSettings_whenSettingsDoNotExist() {
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.empty());

        ExtensionSettings result = getExtensionSettingsUseCase.execute(2L);

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getPauseIntervalMinutes()).isEqualTo(20);
        assertThat(result.isDataSharingConsent()).isFalse();
        assertThat(result.getGardenUrl()).isEqualTo("http://localhost:5173/garden");
        assertThat(result.getBackendUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.getMonitoredDomains()).contains("twitter.com", "x.com", "instagram.com");
    }
}
