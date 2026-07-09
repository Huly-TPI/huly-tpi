package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.ExtensionMetricItem;
import com.huly.backend.domain.dto.extension.SaveExtensionMetricsRequest;
import com.huly.backend.domain.mapper.extension.SaveExtensionMetricsMapper;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveExtensionMetricsUseCaseTest {

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    private SaveExtensionMetricsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveExtensionMetricsUseCase(
                metricsRepository, settingsRepository, new SaveExtensionMetricsMapper());
    }

    @Test
    @DisplayName("Guarda las métricas cuando el usuario dio su consentimiento")
    void executeSavesMetricsWhenUserHasConsented() {
        givenConsentedUser(1L);

        saveMetrics(1L, "twitter.com", 120);

        thenMetricsSaved(1L, "twitter.com", 120);
    }

    @Test
    @DisplayName("No guarda las métricas cuando el usuario no dio su consentimiento")
    void executeDoesNotSaveMetricsWhenUserHasNotConsented() {
        givenNonConsentedUser(1L);

        saveMetrics(1L, "twitter.com", 120);

        thenNoMetricsSaved();
    }

    @Test
    @DisplayName("No guarda las métricas cuando el usuario no tiene opciones almacenadas")
    void executeDoesNotSaveMetricsWhenUserSettingsDoNotExist() {
        givenNoStoredSettings(1L);

        saveMetrics(1L, "twitter.com", 120);

        thenNoMetricsSaved();
    }

    // --- arrange ---

    private void givenConsentedUser(long userId) {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder().dataSharingConsent(true).build();
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void givenNonConsentedUser(long userId) {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder().dataSharingConsent(false).build();
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void givenNoStoredSettings(long userId) {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private void saveMetrics(long userId, String domain, int activeSeconds) {
        useCase.execute(new SaveExtensionMetricsRequest(userId, List.of(
                new ExtensionMetricItem(domain, activeSeconds, 5, 1, 0)
        )));
    }

    // --- assert ---

    private void thenMetricsSaved(long userId, String domain, int activeSeconds) {
        verify(metricsRepository).saveAll(eq(userId), argThat((List<ExtensionMetric> metrics) ->
                metrics.size() == 1
                        && metrics.get(0).getDomain().equals(domain)
                        && metrics.get(0).getActiveSeconds() == activeSeconds));
    }

    private void thenNoMetricsSaved() {
        verify(metricsRepository, never()).saveAll(anyLong(), anyList());
    }
}
