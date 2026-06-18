package com.huly.backend.domain.useCase.admin.userAntiScroll;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserAntiScrollStatsUseCaseTest {

    private UserRepository userRepository;
    private UserAntiScrollSettingsRepository settingsRepository;
    private ExtensionMetricsRepository metricsRepository;
    private GetUserAntiScrollStatsUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(UserAntiScrollSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        useCase = new GetUserAntiScrollStatsUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserAntiScrollStatsRequest(1L, "current", "all")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void execute_shouldReturnEmptyMetrics_whenConsentIsFalse() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(false)
                .monitoredDomains(List.of("facebook.com", "reddit.com"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.antiScrollEnabled()).isTrue();
        assertThat(response.dataSharingConsent()).isFalse();
        assertThat(response.mostUsedApp()).isNull();
        assertThat(response.mostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldNormalizeDomainsAndFilterByMonitoredDomains() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("youtube.com", "instagram.com"))
                .build();

        Instant now = Instant.now();
        ExtensionMetric metric1 = ExtensionMetric.builder()
                .domain("music.youtube.com")
                .activeSeconds(120)
                .createdAt(now)
                .build();

        ExtensionMetric metric2 = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(300)
                .createdAt(now)
                .build();

        ExtensionMetric metric3 = ExtensionMetric.builder()
                .domain("www.reddit.com")
                .activeSeconds(60)
                .createdAt(now)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric1, metric2, metric3));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(420);
        assertThat(response.mostUsedApp()).isEqualTo("instagram.com");
        assertThat(response.topApps()).hasSize(2);
        assertThat(response.topApps().get(0).getDomain()).isEqualTo("instagram.com");
        assertThat(response.topApps().get(1).getDomain()).isEqualTo("youtube.com");
    }

    @Test
    void execute_shouldHandleNullAndEmptyMetricsList() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("youtube.com"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(null);

        GetUserAntiScrollStatsResponse responseNull = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));
        assertThat(responseNull.mostUsedApp()).isNull();

        when(metricsRepository.findByUserId(userId)).thenReturn(List.of());
        GetUserAntiScrollStatsResponse responseEmpty = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));
        assertThat(responseEmpty.mostUsedApp()).isNull();
    }

    @Test
    void execute_shouldSkipMetric_whenCreatedAtIsNull() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("youtube.com"))
                .build();

        ExtensionMetric metricNullDate = ExtensionMetric.builder()
                .domain("youtube.com")
                .activeSeconds(120)
                .createdAt(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metricNullDate));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
    }

    @Test
    void execute_shouldFilterByRequestedWeekAndDay() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com", "reddit.com"))
                .build();

        ZonedDateTime startOfWeek = ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant tuesday = startOfWeek.plusDays(1).plusHours(1).toInstant();
        Instant thursday = startOfWeek.plusDays(3).plusHours(1).toInstant();

        ExtensionMetric currentFacebook = ExtensionMetric.builder()
                .domain("www.facebook.com")
                .activeSeconds(10)
                .createdAt(tuesday)
                .build();
        ExtensionMetric currentReddit = ExtensionMetric.builder()
                .domain("www.reddit.com")
                .activeSeconds(20)
                .createdAt(tuesday)
                .build();
        ExtensionMetric currentThursday = ExtensionMetric.builder()
                .domain("www.reddit.com")
                .activeSeconds(50)
                .createdAt(thursday)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(currentFacebook, currentReddit, currentThursday));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "1"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(30);
        assertThat(response.topApps()).extracting("domain").containsExactly("reddit.com", "facebook.com");
        assertThat(response.topApps()).extracting("totalActiveSeconds").containsExactly(20, 10);
        assertThat(response.dailyScrollTimeSeconds().get("current_1")).isEqualTo(30);
        assertThat(response.dailyScrollTimeSeconds().get("current_3")).isEqualTo(50);
    }
}
