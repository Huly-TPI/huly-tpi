package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.ExtensionMetricItem;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.mapper.extension.SaveExtensionMetricsMapper;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveExtensionMetricsUseCaseTest {

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    private SaveExtensionMetricsUseCase saveExtensionMetricsUseCase;

    @BeforeEach
    void setUp() {
        saveExtensionMetricsUseCase = new SaveExtensionMetricsUseCase(
                metricsRepository, settingsRepository, new SaveExtensionMetricsMapper());
    }

    @Test
    void execute_shouldSaveMetrics_whenUserHasConsented() {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .dataSharingConsent(true)
                .build();
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        SaveExtensionMetricsRequest request = new SaveExtensionMetricsRequest(1L, List.of(
                new ExtensionMetricItem("twitter.com", 120, 5, 1, 0)
        ));

        saveExtensionMetricsUseCase.execute(request);

        verify(metricsRepository).saveAll(eq(1L), argThat((List<ExtensionMetric> metrics) ->
                metrics.size() == 1 && metrics.get(0).getDomain().equals("twitter.com")
                        && metrics.get(0).getActiveSeconds() == 120));
    }

    @Test
    void execute_shouldNotSaveMetrics_whenUserHasNotConsented() {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .dataSharingConsent(false)
                .build();
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        SaveExtensionMetricsRequest request = new SaveExtensionMetricsRequest(1L, List.of(
                new ExtensionMetricItem("twitter.com", 120, 0, 0, 0)
        ));

        saveExtensionMetricsUseCase.execute(request);

        verify(metricsRepository, never()).saveAll(anyLong(), anyList());
    }

    @Test
    void execute_shouldNotSaveMetrics_whenUserSettingsDoNotExist() {
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        SaveExtensionMetricsRequest request = new SaveExtensionMetricsRequest(1L, List.of(
                new ExtensionMetricItem("twitter.com", 120, 0, 0, 0)
        ));

        saveExtensionMetricsUseCase.execute(request);

        verify(metricsRepository, never()).saveAll(anyLong(), anyList());
    }
}
