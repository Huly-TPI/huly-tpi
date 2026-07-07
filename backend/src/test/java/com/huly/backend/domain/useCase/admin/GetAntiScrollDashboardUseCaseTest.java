package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.AntiScrollDashboardStats;
import com.huly.backend.domain.model.extension.ExtensionMetric;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAntiScrollDashboardUseCaseTest {

    private static final Long USER_1 = 2L;
    private static final Long USER_2 = 3L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @InjectMocks
    private GetAntiScrollDashboardUseCase useCase;

    @Test
    @DisplayName("Agrega usuarios, consentimiento y métricas cuando hay datos")
    void executeShouldAggregateStatsWhenUsersAndMetricsExist() {
        // --- arrange ---
        givenNonAdmins(activeUser(USER_1), activeUser(USER_2));
        givenSettings(USER_1, settings(true, true));
        givenSettings(USER_2, settings(false, false));
        givenConsentingMetrics(
                metric("instagram.com", 1000, 5, 2),
                metric("twitter.com", 2000, 10, 4));

        // --- act ---
        AntiScrollDashboardStats stats = dashboard();

        // --- assert ---
        thenTwoUserAggregate(stats);
    }

    @Test
    @DisplayName("Ignora en los contadores a los usuarios sin configuración de anti-scroll")
    void executeShouldIgnoreUsersWithoutSettings() {
        // --- arrange ---
        givenNonAdmins(activeUser(USER_1));
        givenNoSettings(USER_1);
        givenNoConsentingMetrics();

        // --- act ---
        AntiScrollDashboardStats stats = dashboard();

        // --- assert ---
        thenUserCountedButNotActiveNorConsenting(stats);
    }

    @Test
    @DisplayName("Devuelve estadísticas en cero cuando no hay usuarios ni métricas")
    void executeShouldReturnZeroStatsWhenNoUsersNorMetrics() {
        // --- arrange ---
        givenNoNonAdmins();
        givenNoConsentingMetrics();

        // --- act ---
        AntiScrollDashboardStats stats = dashboard();

        // --- assert ---
        thenZeroStats(stats);
    }

    // --- arrange ---

    private void givenNonAdmins(AppUser... users) {
        when(userRepository.findAllNonAdmins()).thenReturn(List.of(users));
    }

    private void givenNoNonAdmins() {
        when(userRepository.findAllNonAdmins()).thenReturn(List.of());
    }

    private void givenSettings(Long userId, UserAntiScrollSettings settings) {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void givenNoSettings(Long userId) {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
    }

    private void givenConsentingMetrics(ExtensionMetric... metrics) {
        when(metricsRepository.findAllConsentingMetrics()).thenReturn(List.of(metrics));
    }

    private void givenNoConsentingMetrics() {
        when(metricsRepository.findAllConsentingMetrics()).thenReturn(List.of());
    }

    private AppUser activeUser(Long id) {
        return AppUser.builder().id(id).role(UserRole.USER).status(UserStatus.ACTIVE).build();
    }

    private UserAntiScrollSettings settings(boolean enabled, boolean consent) {
        return UserAntiScrollSettings.builder().enabled(enabled).dataSharingConsent(consent).build();
    }

    private ExtensionMetric metric(String domain, int activeSeconds, int modalsShown, int redirects) {
        return ExtensionMetric.builder()
                .domain(domain)
                .activeSeconds(activeSeconds)
                .modalsShown(modalsShown)
                .redirects(redirects)
                .build();
    }

    // --- act ---

    private AntiScrollDashboardStats dashboard() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenTwoUserAggregate(AntiScrollDashboardStats stats) {
        assertThat(stats.getTotalUsersCount()).isEqualTo(2);
        assertThat(stats.getActiveExtensionUsersCount()).isEqualTo(1);
        assertThat(stats.getDataSharingConsentUsersCount()).isEqualTo(1);
        assertThat(stats.getTotalModalsShown()).isEqualTo(15);
        assertThat(stats.getTotalRedirects()).isEqualTo(6);
        assertThat(stats.getTopUsedApps()).hasSize(2);
        assertThat(stats.getTopUsedApps().get(0).getDomain()).isEqualTo("twitter.com");
        assertThat(stats.getTopUsedApps().get(0).getTotalActiveSeconds()).isEqualTo(2000);
        assertThat(stats.getTopUsedApps().get(1).getDomain()).isEqualTo("instagram.com");
        assertThat(stats.getTopUsedApps().get(1).getTotalActiveSeconds()).isEqualTo(1000);
    }

    private void thenUserCountedButNotActiveNorConsenting(AntiScrollDashboardStats stats) {
        assertThat(stats.getTotalUsersCount()).isEqualTo(1);
        assertThat(stats.getActiveExtensionUsersCount()).isEqualTo(0);
        assertThat(stats.getDataSharingConsentUsersCount()).isEqualTo(0);
        assertThat(stats.getTopUsedApps()).isEmpty();
    }

    private void thenZeroStats(AntiScrollDashboardStats stats) {
        assertThat(stats.getTotalUsersCount()).isEqualTo(0);
        assertThat(stats.getActiveExtensionUsersCount()).isEqualTo(0);
        assertThat(stats.getDataSharingConsentUsersCount()).isEqualTo(0);
        assertThat(stats.getTotalModalsShown()).isEqualTo(0);
        assertThat(stats.getTotalRedirects()).isEqualTo(0);
        assertThat(stats.getTopUsedApps()).isEmpty();
    }
}
