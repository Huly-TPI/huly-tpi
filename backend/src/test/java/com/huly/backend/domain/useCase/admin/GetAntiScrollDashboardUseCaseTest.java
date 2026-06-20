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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetAntiScrollDashboardUseCaseTest {

    private UserRepository userRepository;
    private UserAntiScrollSettingsRepository settingsRepository;
    private ExtensionMetricsRepository metricsRepository;
    private GetAntiScrollDashboardUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(UserAntiScrollSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        useCase = new GetAntiScrollDashboardUseCase(userRepository, settingsRepository, metricsRepository);
    }

    @Test
    void execute_shouldAggregateCorrectly() {
        AppUser user1 = AppUser.builder().id(2L).role(UserRole.USER).status(UserStatus.ACTIVE).build();
        AppUser user2 = AppUser.builder().id(3L).role(UserRole.USER).status(UserStatus.ACTIVE).build();

        UserAntiScrollSettings settings1 = UserAntiScrollSettings.builder().enabled(true).dataSharingConsent(true).build();
        UserAntiScrollSettings settings2 = UserAntiScrollSettings.builder().enabled(false).dataSharingConsent(false).build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user1, user2));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings1));
        when(settingsRepository.findByUserId(3L)).thenReturn(Optional.of(settings2));

        ExtensionMetric metric1 = ExtensionMetric.builder().domain("instagram.com").activeSeconds(1000).modalsShown(5).redirects(2).build();
        ExtensionMetric metric2 = ExtensionMetric.builder().domain("twitter.com").activeSeconds(2000).modalsShown(10).redirects(4).build();

        when(metricsRepository.findAllConsentingMetrics()).thenReturn(List.of(metric1, metric2));

        AntiScrollDashboardStats stats = useCase.execute();

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
}
