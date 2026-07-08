package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsRequest;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsResponse;
import com.huly.backend.domain.useCase.admin.userAntiScroll.GetUserAntiScrollStatsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserAntiScrollStatsUseCaseTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @InjectMocks
    private GetUserAntiScrollStatsUseCase useCase;

    @Test
    @DisplayName("Lanza excepción cuando el usuario no existe")
    void executeShouldThrowWhenUserNotFound() {
        // --- arrange ---
        givenUserNotFound();

        // --- assert ---
        thenStatsThrowsUserNotFound();
    }

    @Test
    @DisplayName("Devuelve métricas vacías cuando no hay consentimiento de datos")
    void executeShouldReturnEmptyMetricsWhenConsentIsFalse() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, false, List.of("facebook.com", "reddit.com")));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenEmptyStatsWithConsentFalse(response);
    }

    @Test
    @DisplayName("Normaliza dominios y filtra por los dominios monitoreados")
    void executeShouldNormalizeDomainsAndFilterByMonitoredDomains() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("youtube.com", "instagram.com")));
        givenMetrics(
                metricNow("music.youtube.com", 120),
                metricNow("instagram.com", 300),
                metricNow("www.reddit.com", 60));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenNormalizedAndFiltered(response);
    }

    @Test
    @DisplayName("Devuelve app más usada nula cuando la lista de métricas es null")
    void executeShouldHandleNullMetricsList() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("youtube.com")));
        givenNullMetrics();

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenMostUsedAppIsNull(response);
    }

    @Test
    @DisplayName("Devuelve app más usada nula cuando la lista de métricas está vacía")
    void executeShouldHandleEmptyMetricsList() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("youtube.com")));
        givenNoMetrics();

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenMostUsedAppIsNull(response);
    }

    @Test
    @DisplayName("Omite la métrica cuando su fecha de creación es null")
    void executeShouldSkipMetricWhenCreatedAtIsNull() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("youtube.com")));
        givenMetrics(metric("youtube.com", 120, null));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalScrollTime(response, 0);
    }

    @Test
    @DisplayName("Devuelve valores deshabilitados por defecto cuando no existe configuración")
    void executeShouldReturnDisabledDefaultsWhenSettingsDoNotExist() {
        // --- arrange ---
        givenUserExists();
        givenNoSettings();

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenDisabledDefaults(response);
    }

    @Test
    @DisplayName("Filtra por la semana y el día solicitados")
    void executeShouldFilterByRequestedWeekAndDay() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com", "reddit.com")));
        givenMetrics(
                metric("www.facebook.com", 10, dayOfThisWeek(1)),
                metric("www.reddit.com", 20, dayOfThisWeek(1)),
                metric("www.reddit.com", 50, dayOfThisWeek(3)));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "1");

        // --- assert ---
        thenWeekAndDayFilter(response);
    }

    @Test
    @DisplayName("Filtra por la semana anterior")
    void executeShouldFilterByPreviousWeek() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com")));
        givenMetrics(
                metric("facebook.com", 25, dayOfPreviousWeek(1)),
                metric("facebook.com", 10, dayOfThisWeek(1)));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("previous", "all");

        // --- assert ---
        thenPreviousWeekFilter(response);
    }

    @Test
    @DisplayName("Acepta un día null como la semana completa")
    void executeShouldAcceptNullDayAsWholeWeek() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com", "reddit.com")));
        givenMetrics(
                metric("facebook.com", 15, dayOfThisWeek(1)),
                metric("reddit.com", 35, dayOfThisWeek(3)));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", null);

        // --- assert ---
        thenWholeWeekTotalAndTopApps(response, 50);
    }

    @Test
    @DisplayName("Acepta un día inválido como la semana completa")
    void executeShouldAcceptInvalidDayAsWholeWeek() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com")));
        givenMetrics(metric("facebook.com", 18, dayOfThisWeek(2)));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "abc");

        // --- assert ---
        thenTotalAndSingleDomain(response, 18, "facebook.com");
    }

    @Test
    @DisplayName("Ignora métricas fuera de la ventana de la semana solicitada")
    void executeShouldIgnoreMetricsOutsideRequestedWeekWindow() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com")));
        givenMetrics(metric("facebook.com", 40, weeksAgo(3, 1)));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalZeroAndEmptyTopApps(response);
    }

    @Test
    @DisplayName("Devuelve estadísticas vacías cuando los dominios monitoreados están vacíos")
    void executeShouldReturnEmptyStatsWhenMonitoredDomainsAreEmpty() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of()));
        givenMetrics(metricNow("facebook.com", 10));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalZeroAndEmptyTopApps(response);
    }

    @Test
    @DisplayName("Ignora la métrica cuando su dominio es null")
    void executeShouldIgnoreMetricWhenDomainIsNull() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com")));
        givenMetrics(metricNow(null, 10));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalZeroAndEmptyTopApps(response);
    }

    @Test
    @DisplayName("Devuelve estadísticas vacías cuando los dominios monitoreados son null")
    void executeShouldReturnEmptyStatsWhenMonitoredDomainsAreNull() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, null));
        givenMetrics(metricNow("facebook.com", 10));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalZeroAndEmptyTopApps(response);
    }

    @Test
    @DisplayName("Maneja entradas de dominios monitoreados nulas durante la normalización")
    void executeShouldHandleNullMonitoredDomainEntriesDuringNormalization() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, Arrays.asList(null, "facebook.com")));
        givenMetrics(metricNow("facebook.com", 10));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalAndSingleDomain(response, 10, "facebook.com");
    }

    @Test
    @DisplayName("Normaliza dominios con www, puerto, punto final y TLD de país")
    void executeShouldNormalizeDomainsWithWwwPortTrailingDotAndCountryCodeTld() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("youtube.com", "reddit.com", "bbc.co.uk")));
        givenMetrics(
                metricNow("www.youtube.com", 12),
                metricNow("reddit.com:3000", 8),
                metricNow("news.bbc.co.uk.", 20));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTopAppsDomainsExactly(response, 40, "bbc.co.uk", "youtube.com", "reddit.com");
    }

    @Test
    @DisplayName("Normaliza dominios null, localhost, IP y etiquetas en blanco")
    void executeShouldNormalizeNullLocalhostIpAndBlankLabels() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("", "localhost", "127.0.0.1", "foo.bar.com")));
        givenMetrics(
                metricNow(null, 5),
                metricNow("localhost", 7),
                metricNow("127.0.0.1", 9),
                metricNow("foo..bar.com", 11));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTopAppsDomainsExactly(response, 27, "bar.com", "127.0.0.1", "localhost");
    }

    @Test
    @DisplayName("Maneja un TLD de dos letras fuera de la lista de segundo nivel configurada")
    void executeShouldHandleTwoLetterTldOutsideConfiguredSecondLevelList() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("service.custom.zz")));
        givenMetrics(metricNow("api.service.custom.zz", 14));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalAndSingleDomain(response, 14, "custom.zz");
    }

    @Test
    @DisplayName("Ignora la métrica exactamente en el inicio de la semana siguiente en los buckets diarios")
    void executeShouldIgnoreMetricExactlyAtStartOfNextWeekInDailyBuckets() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("facebook.com")));
        givenMetrics(metric("facebook.com", 22, startOfNextWeek()));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalZeroAndAllDailyZero(response);
    }

    @Test
    @DisplayName("matchesMonitoredDomains devuelve false cuando la lista monitoreada es null")
    void matchesMonitoredDomainsShouldReturnFalseWhenMonitoredDomainsArgumentIsNull() {
        // --- act ---
        boolean result = invokeMatchesMonitoredDomains("facebook.com", null);

        // --- assert ---
        thenMatchResultIsFalse(result);
    }

    @Test
    @DisplayName("Coincide dominios con sufijo de país durante la ejecución")
    void executeShouldMatchDomainsWithCountrySuffix() {
        // --- arrange ---
        givenUserExists();
        givenSettings(settings(true, true, List.of("mercadolibre.com", "google.com.ar")));
        givenMetrics(
                metricNow("www.mercadolibre.com.ar", 100),
                metricNow("google.com", 200));

        // --- act ---
        GetUserAntiScrollStatsResponse response = statsFor("current", "all");

        // --- assert ---
        thenTotalAndTopAppsSize(response, 300, 2);
    }

    @Test
    @DisplayName("matchesMonitoredDomains coincide dominios con sufijo de país")
    void matchesMonitoredDomainsShouldMatchDomainsWithCountrySuffix() {
        // --- arrange ---
        List<String> monitored = List.of("mercadolibre.com", "google.com.ar");

        // --- act ---
        boolean matchMercadoLibre = invokeMatchesMonitoredDomains("www.mercadolibre.com.ar", monitored);
        boolean matchGoogle = invokeMatchesMonitoredDomains("google.com", monitored);
        boolean matchWikipedia = invokeMatchesMonitoredDomains("wikipedia.org", monitored);

        // --- assert ---
        thenCountrySuffixMatches(matchMercadoLibre, matchGoogle, matchWikipedia);
    }

    // --- arrange ---

    private void givenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
    }

    private void givenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenSettings(UserAntiScrollSettings settings) {
        when(settingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(settings));
    }

    private void givenNoSettings() {
        when(settingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenMetrics(ExtensionMetric... metrics) {
        when(metricsRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(metrics));
    }

    private void givenNoMetrics() {
        when(metricsRepository.findByUserId(USER_ID)).thenReturn(List.of());
    }

    private void givenNullMetrics() {
        when(metricsRepository.findByUserId(USER_ID)).thenReturn(null);
    }

    private UserAntiScrollSettings settings(boolean enabled, boolean consent, List<String> monitoredDomains) {
        return UserAntiScrollSettings.builder()
                .enabled(enabled)
                .dataSharingConsent(consent)
                .monitoredDomains(monitoredDomains)
                .build();
    }

    private ExtensionMetric metric(String domain, int activeSeconds, Instant createdAt) {
        return ExtensionMetric.builder()
                .domain(domain)
                .activeSeconds(activeSeconds)
                .createdAt(createdAt)
                .build();
    }

    private ExtensionMetric metricNow(String domain, int activeSeconds) {
        return metric(domain, activeSeconds, Instant.now());
    }

    private ZonedDateTime startOfWeek() {
        return ZonedDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS);
    }

    private Instant dayOfThisWeek(int dayOffset) {
        return startOfWeek().plusDays(dayOffset).plusHours(1).toInstant();
    }

    private Instant dayOfPreviousWeek(int dayOffset) {
        return startOfWeek().minusWeeks(1).plusDays(dayOffset).plusHours(1).toInstant();
    }

    private Instant startOfNextWeek() {
        return startOfWeek().plusWeeks(1).toInstant();
    }

    private Instant weeksAgo(int weeks, int dayOffset) {
        return startOfWeek().minusWeeks(weeks).plusDays(dayOffset).toInstant();
    }

    // --- act ---

    private GetUserAntiScrollStatsResponse statsFor(String week, String day) {
        return useCase.execute(new GetUserAntiScrollStatsRequest(USER_ID, week, day));
    }

    private boolean invokeMatchesMonitoredDomains(String metricDomain, List<String> monitoredDomains) {
        try {
            Method method = GetUserAntiScrollStatsUseCase.class.getDeclaredMethod(
                    "matchesMonitoredDomains", String.class, List.class);
            method.setAccessible(true);
            return (boolean) method.invoke(useCase, metricDomain, monitoredDomains);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // --- assert ---

    private void thenStatsThrowsUserNotFound() {
        assertThatThrownBy(() -> statsFor("current", "all"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    private void thenEmptyStatsWithConsentFalse(GetUserAntiScrollStatsResponse response) {
        assertThat(response.antiScrollEnabled()).isTrue();
        assertThat(response.dataSharingConsent()).isFalse();
        assertThat(response.mostUsedApp()).isNull();
        assertThat(response.mostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    private void thenNormalizedAndFiltered(GetUserAntiScrollStatsResponse response) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(420);
        assertThat(response.mostUsedApp()).isEqualTo("instagram.com");
        assertThat(response.topApps()).hasSize(2);
        assertThat(response.topApps().get(0).getDomain()).isEqualTo("instagram.com");
        assertThat(response.topApps().get(1).getDomain()).isEqualTo("youtube.com");
    }

    private void thenMostUsedAppIsNull(GetUserAntiScrollStatsResponse response) {
        assertThat(response.mostUsedApp()).isNull();
    }

    private void thenTotalScrollTime(GetUserAntiScrollStatsResponse response, int expectedTotal) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(expectedTotal);
    }

    private void thenDisabledDefaults(GetUserAntiScrollStatsResponse response) {
        assertThat(response.antiScrollEnabled()).isFalse();
        assertThat(response.dataSharingConsent()).isFalse();
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    private void thenWeekAndDayFilter(GetUserAntiScrollStatsResponse response) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(30);
        assertThat(response.topApps()).extracting("domain").containsExactly("reddit.com", "facebook.com");
        assertThat(response.topApps()).extracting("totalActiveSeconds").containsExactly(20, 10);
        assertThat(response.dailyScrollTimeSeconds().get("current_1")).isEqualTo(30);
        assertThat(response.dailyScrollTimeSeconds().get("current_3")).isEqualTo(50);
    }

    private void thenPreviousWeekFilter(GetUserAntiScrollStatsResponse response) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(25);
        assertThat(response.mostUsedApp()).isEqualTo("facebook.com");
        assertThat(response.dailyScrollTimeSeconds().get("previous_1")).isEqualTo(25);
        assertThat(response.dailyScrollTimeSeconds().get("current_1")).isEqualTo(10);
    }

    private void thenWholeWeekTotalAndTopApps(GetUserAntiScrollStatsResponse response, int expectedTotal) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(expectedTotal);
        assertThat(response.topApps()).extracting("domain").containsExactly("reddit.com", "facebook.com");
    }

    private void thenTotalAndSingleDomain(GetUserAntiScrollStatsResponse response, int expectedTotal, String domain) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(expectedTotal);
        assertThat(response.topApps()).singleElement().extracting("domain").isEqualTo(domain);
    }

    private void thenTotalZeroAndEmptyTopApps(GetUserAntiScrollStatsResponse response) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.topApps()).isEmpty();
    }

    private void thenTopAppsDomainsExactly(GetUserAntiScrollStatsResponse response, int expectedTotal, String... domains) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(expectedTotal);
        assertThat(response.topApps()).extracting("domain").containsExactly((Object[]) domains);
    }

    private void thenTotalZeroAndAllDailyZero(GetUserAntiScrollStatsResponse response) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(0);
        assertThat(response.dailyScrollTimeSeconds().values()).allMatch(value -> value == 0);
    }

    private void thenTotalAndTopAppsSize(GetUserAntiScrollStatsResponse response, int expectedTotal, int expectedSize) {
        assertThat(response.totalScrollTimeSeconds()).isEqualTo(expectedTotal);
        assertThat(response.topApps()).hasSize(expectedSize);
    }

    private void thenMatchResultIsFalse(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenCountrySuffixMatches(boolean matchMercadoLibre, boolean matchGoogle, boolean matchWikipedia) {
        assertThat(matchMercadoLibre).isTrue();
        assertThat(matchGoogle).isTrue();
        assertThat(matchWikipedia).isFalse();
    }
}
