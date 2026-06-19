package com.huly.backend.domain.useCase.admin.userAntiScroll;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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
    void execute_shouldReturnDisabledDefaults_whenSettingsDoNotExist() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.antiScrollEnabled()).isFalse();
        assertThat(response.dataSharingConsent()).isFalse();
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
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

    @Test
    void execute_shouldFilterByPreviousWeek() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com"))
                .build();

        ZonedDateTime startOfWeek = ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant previousTuesday = startOfWeek.minusWeeks(1).plusDays(1).plusHours(1).toInstant();
        Instant currentTuesday = startOfWeek.plusDays(1).plusHours(1).toInstant();

        ExtensionMetric previousMetric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(25)
                .createdAt(previousTuesday)
                .build();
        ExtensionMetric currentMetric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(10)
                .createdAt(currentTuesday)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(previousMetric, currentMetric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "previous", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(25);
        assertThat(response.mostUsedApp()).isEqualTo("facebook.com");
        assertThat(response.dailyScrollTimeSeconds().get("previous_1")).isEqualTo(25);
        assertThat(response.dailyScrollTimeSeconds().get("current_1")).isEqualTo(10);
    }

    @Test
    void execute_shouldAcceptNullDayAsWholeWeek() {
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

        ExtensionMetric tuesdayMetric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(15)
                .createdAt(startOfWeek.plusDays(1).plusHours(1).toInstant())
                .build();
        ExtensionMetric thursdayMetric = ExtensionMetric.builder()
                .domain("reddit.com")
                .activeSeconds(35)
                .createdAt(startOfWeek.plusDays(3).plusHours(1).toInstant())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(tuesdayMetric, thursdayMetric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", null));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(50);
        assertThat(response.topApps()).extracting("domain").containsExactly("reddit.com", "facebook.com");
    }

    @Test
    void execute_shouldAcceptInvalidDayAsWholeWeek() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com"))
                .build();

        ZonedDateTime startOfWeek = ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(18)
                .createdAt(startOfWeek.plusDays(2).plusHours(1).toInstant())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "abc"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(18);
        assertThat(response.topApps()).singleElement().extracting("domain").isEqualTo("facebook.com");
    }

    @Test
    void execute_shouldIgnoreMetricsOutsideRequestedWeekWindow() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com"))
                .build();

        ZonedDateTime startOfWeek = ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        ExtensionMetric oldMetric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(40)
                .createdAt(startOfWeek.minusWeeks(3).plusDays(1).toInstant())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(oldMetric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldReturnEmptyStatsWhenMonitoredDomainsAreEmpty() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of())
                .build();

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(10)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldIgnoreMetricWhenDomainIsNull() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com"))
                .build();

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain(null)
                .activeSeconds(10)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldReturnEmptyStatsWhenMonitoredDomainsAreNull() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(null)
                .build();

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(10)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    @Test
    void execute_shouldHandleNullMonitoredDomainEntriesDuringNormalization() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(java.util.Arrays.asList(null, "facebook.com"))
                .build();

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(10)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(10);
        assertThat(response.topApps()).singleElement().extracting("domain").isEqualTo("facebook.com");
    }

    @Test
    void execute_shouldNormalizeDomainsWithWwwPortTrailingDotAndCountryCodeTld() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("youtube.com", "reddit.com", "bbc.co.uk"))
                .build();

        Instant now = Instant.now();
        ExtensionMetric youtubeMetric = ExtensionMetric.builder()
                .domain("www.youtube.com")
                .activeSeconds(12)
                .createdAt(now)
                .build();
        ExtensionMetric redditMetric = ExtensionMetric.builder()
                .domain("reddit.com:3000")
                .activeSeconds(8)
                .createdAt(now)
                .build();
        ExtensionMetric bbcMetric = ExtensionMetric.builder()
                .domain("news.bbc.co.uk.")
                .activeSeconds(20)
                .createdAt(now)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(youtubeMetric, redditMetric, bbcMetric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(40);
        assertThat(response.topApps()).extracting("domain").containsExactly("bbc.co.uk", "youtube.com", "reddit.com");
    }

    @Test
    void execute_shouldNormalizeNullLocalhostIpAndBlankLabels() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("", "localhost", "127.0.0.1", "foo.bar.com"))
                .build();

        Instant now = Instant.now();
        ExtensionMetric nullDomainMetric = ExtensionMetric.builder()
                .domain(null)
                .activeSeconds(5)
                .createdAt(now)
                .build();
        ExtensionMetric localhostMetric = ExtensionMetric.builder()
                .domain("localhost")
                .activeSeconds(7)
                .createdAt(now)
                .build();
        ExtensionMetric ipMetric = ExtensionMetric.builder()
                .domain("127.0.0.1")
                .activeSeconds(9)
                .createdAt(now)
                .build();
        ExtensionMetric blankLabelMetric = ExtensionMetric.builder()
                .domain("foo..bar.com")
                .activeSeconds(11)
                .createdAt(now)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(
                nullDomainMetric,
                localhostMetric,
                ipMetric,
                blankLabelMetric
        ));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(27);
        assertThat(response.topApps()).extracting("domain").containsExactly("bar.com", "127.0.0.1", "localhost");
    }

    @Test
    void execute_shouldHandleTwoLetterTldOutsideConfiguredSecondLevelList() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("service.custom.zz"))
                .build();

        ExtensionMetric metric = ExtensionMetric.builder()
                .domain("api.service.custom.zz")
                .activeSeconds(14)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(metric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(14);
        assertThat(response.topApps()).singleElement().extracting("domain").isEqualTo("custom.zz");
    }

    @Test
    void execute_shouldIgnoreMetricExactlyAtStartOfNextWeekInDailyBuckets() {
        Long userId = 1L;
        AppUser user = AppUser.builder().id(userId).build();
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .monitoredDomains(List.of("facebook.com"))
                .build();

        ZonedDateTime startOfWeek = ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        ExtensionMetric nextWeekBoundaryMetric = ExtensionMetric.builder()
                .domain("facebook.com")
                .activeSeconds(22)
                .createdAt(startOfWeek.plusWeeks(1).toInstant())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(metricsRepository.findByUserId(userId)).thenReturn(List.of(nextWeekBoundaryMetric));

        GetUserAntiScrollStatsResponse response = useCase.execute(new GetUserAntiScrollStatsRequest(userId, "current", "all"));

        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.dailyScrollTimeSeconds().values()).allMatch(value -> value == 0);
    }

    @Test
    void matchesMonitoredDomains_shouldReturnFalseWhenMonitoredDomainsArgumentIsNull() throws Exception {
        Method method = GetUserAntiScrollStatsUseCase.class.getDeclaredMethod(
                "matchesMonitoredDomains",
                String.class,
                List.class
        );
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(useCase, "facebook.com", null);

        assertThat(result).isFalse();
    }
}
