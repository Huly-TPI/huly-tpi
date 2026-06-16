package com.huly.backend.domain.useCase.admin.userAntiScroll;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserAntiScrollStatsUseCaseTest {

    private UserRepository userRepository;
    private ExtensionSettingsRepository settingsRepository;
    private ExtensionMetricsRepository metricsRepository;
    private GetUserAntiScrollStatsUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(ExtensionSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        useCase = new GetUserAntiScrollStatsUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserAntiScrollStatsRequest(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void execute_shouldReturnEmptyMetrics_whenConsentIsFalse() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId));

        assertThat(response.antiScrollEnabled()).isTrue();
        assertThat(response.dataSharingConsent()).isFalse();
        assertThat(response.mostUsedApp()).isNull();
        assertThat(response.mostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldReturnCalculatedMetrics_whenConsentIsTrue() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        Instant now = Instant.now();
        ExtensionMetric metric1 = ExtensionMetric.builder()
                .domain("youtube.com")
                .activeSeconds(120)
                .createdAt(now)
                .build();

        ExtensionMetric metric2 = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(300)
                .createdAt(now)
                .build();

        ExtensionMetric metric3 = ExtensionMetric.builder()
                .domain("youtube.com")
                .activeSeconds(60)
                .createdAt(now.minus(7, ChronoUnit.DAYS))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric1, metric2, metric3));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId));

        assertThat(response.antiScrollEnabled()).isTrue();
        assertThat(response.dataSharingConsent()).isTrue();
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(480);
        assertThat(response.mostUsedApp()).isEqualTo("instagram.com");
        assertThat(response.mostUsedAppActiveSeconds()).isEqualTo(300);
        assertThat(response.topApps()).hasSize(2);
        assertThat(response.topApps().get(0).getDomain()).isEqualTo("instagram.com");
        assertThat(response.topApps().get(1).getDomain()).isEqualTo("youtube.com");
    }

    @Test
    void execute_shouldHandleNullAndEmptyMetricsList() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(null);

        GetUserAntiScrollStatsResponse responseNull = useCase.execute(new GetUserAntiScrollStatsRequest(userId));
        assertThat(responseNull.mostUsedApp()).isNull();

        when(metricsRepository.findByUserId(userId)).thenReturn(List.of());
        GetUserAntiScrollStatsResponse responseEmpty = useCase.execute(new GetUserAntiScrollStatsRequest(userId));
        assertThat(responseEmpty.mostUsedApp()).isNull();
    }

    @Test
    void execute_shouldSkipMetric_whenCreatedAtIsNull() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        ExtensionMetric metricNullDate = ExtensionMetric.builder()
                .domain("youtube.com")
                .activeSeconds(120)
                .createdAt(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metricNullDate));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId));
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(120);
    }
}
