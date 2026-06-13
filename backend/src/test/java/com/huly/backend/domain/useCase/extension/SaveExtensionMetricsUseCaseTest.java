package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveExtensionMetricsUseCaseTest {

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @Mock
    private ExtensionSettingsRepository settingsRepository;

    @InjectMocks
    private SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;

    @Test
    void execute_shouldSaveMetrics_whenUserHasConsented() {
        ExtensionSettings settings = ExtensionSettings.builder()
                .dataSharingConsent(true)
                .build();
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        List<ExtensionMetric> metrics = List.of(
                ExtensionMetric.builder()
                        .domain("twitter.com")
                        .activeSeconds(120)
                        .scrollCount(5)
                        .modalsShown(1)
                        .redirects(0)
                        .build()
        );

        saveExtensionMetricsUseCase.execute(1L, metrics);

        verify(metricsRepository).saveAll(1L, metrics);
    }

    @Test
    void execute_shouldNotSaveMetrics_whenUserHasNotConsented() {
        ExtensionSettings settings = ExtensionSettings.builder()
                .dataSharingConsent(false)
                .build();
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        List<ExtensionMetric> metrics = List.of(
                ExtensionMetric.builder()
                        .domain("twitter.com")
                        .activeSeconds(120)
                        .build()
        );

        saveExtensionMetricsUseCase.execute(1L, metrics);

        verify(metricsRepository, never()).saveAll(anyLong(), anyList());
    }

    @Test
    void execute_shouldNotSaveMetrics_whenUserSettingsDoNotExist() {
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        List<ExtensionMetric> metrics = List.of(
                ExtensionMetric.builder()
                        .domain("twitter.com")
                        .activeSeconds(120)
                        .build()
        );

        saveExtensionMetricsUseCase.execute(1L, metrics);

        verify(metricsRepository, never()).saveAll(anyLong(), anyList());
    }
}
