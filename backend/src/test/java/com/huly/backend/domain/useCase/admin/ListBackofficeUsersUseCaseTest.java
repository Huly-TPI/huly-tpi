package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListBackofficeUsersUseCaseTest {

    private UserRepository userRepository;
    private ExtensionSettingsRepository settingsRepository;
    private ExtensionMetricsRepository metricsRepository;
    private ListBackofficeUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(ExtensionSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        useCase = new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Test
    void execute_shouldProcessUsersAndSettingsAndMetrics() {
        AppUser user = AppUser.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        ExtensionMetric metric1 = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(1200)
                .build();

        ExtensionMetric metric2 = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(1800)
                .build();

        ExtensionMetric metric3 = ExtensionMetric.builder()
                .domain("twitter.com")
                .activeSeconds(500)
                .build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(2L)).thenReturn(List.of(metric1, metric2, metric3));

        List<BackofficeUserSummary> result = useCase.execute();

        assertThat(result).hasSize(1);
        BackofficeUserSummary summary = result.get(0);
        assertThat(summary.getId()).isEqualTo(2L);
        assertThat(summary.getName()).isEqualTo("John Doe");
        assertThat(summary.isAntiScrollEnabled()).isTrue();
        assertThat(summary.isDataSharingConsent()).isTrue();
        assertThat(summary.getMostUsedApp()).isEqualTo("instagram.com");
        assertThat(summary.getMostUsedAppActiveSeconds()).isEqualTo(3000);
        assertThat(summary.getTotalScrollTimeSeconds()).isEqualTo(3500);
        assertThat(summary.getTopApps()).hasSize(2);
        assertThat(summary.getTopApps().get(0).getDomain()).isEqualTo("instagram.com");
        assertThat(summary.getTopApps().get(0).getTotalActiveSeconds()).isEqualTo(3000);
        assertThat(summary.getTopApps().get(1).getDomain()).isEqualTo("twitter.com");
        assertThat(summary.getTopApps().get(1).getTotalActiveSeconds()).isEqualTo(500);
    }

    @Test
    void execute_shouldCalculateDailyScrollTimeSecondsCorrectly() {
        AppUser user = AppUser.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
        java.time.ZonedDateTime startOfThisWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        java.time.ZonedDateTime startOfPreviousWeek = startOfThisWeek.minusWeeks(1);

        ExtensionMetric metricCurrentMon = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(500)
                .createdAt(startOfThisWeek.toInstant())
                .build();

        ExtensionMetric metricCurrentTue = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(700)
                .createdAt(startOfThisWeek.plusDays(1).toInstant())
                .build();

        ExtensionMetric metricPreviousMon = ExtensionMetric.builder()
                .domain("twitter.com")
                .activeSeconds(300)
                .createdAt(startOfPreviousWeek.toInstant())
                .build();

        ExtensionMetric metricIgnored = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(1000)
                .createdAt(startOfPreviousWeek.minusDays(1).toInstant())
                .build();

        ExtensionMetric metricNullCreatedAt = ExtensionMetric.builder()
                .domain("instagram.com")
                .activeSeconds(100)
                .createdAt(null)
                .build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(2L)).thenReturn(List.of(
                metricCurrentMon, metricCurrentTue, metricPreviousMon, metricIgnored, metricNullCreatedAt
        ));

        List<BackofficeUserSummary> result = useCase.execute();

        assertThat(result).hasSize(1);
        BackofficeUserSummary summary = result.get(0);
        java.util.Map<String, Integer> daily = summary.getDailyScrollTimeSeconds();
        assertThat(daily).isNotNull();
        assertThat(daily.get("current_0")).isEqualTo(500);
        assertThat(daily.get("current_1")).isEqualTo(700);
        assertThat(daily.get("current_2")).isEqualTo(0);
        assertThat(daily.get("previous_0")).isEqualTo(300);
        assertThat(daily.get("previous_1")).isEqualTo(0);
    }

    @Test
    void execute_shouldNotIncludeMetrics_whenConsentIsFalse() {
        AppUser user = AppUser.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        ExtensionSettings settings = ExtensionSettings.builder()
                .enabled(true)
                .dataSharingConsent(false)
                .build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings));

        List<BackofficeUserSummary> result = useCase.execute();

        assertThat(result).hasSize(1);
        BackofficeUserSummary summary = result.get(0);
        assertThat(summary.isAntiScrollEnabled()).isTrue();
        assertThat(summary.isDataSharingConsent()).isFalse();
        assertThat(summary.getMostUsedApp()).isNull();
        assertThat(summary.getMostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(summary.getTotalScrollTimeSeconds()).isEqualTo(0);
        verifyNoInteractions(metricsRepository);
    }
}
