package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.ExtensionSettingsRepository;
import com.huly.backend.domain.repository.UserPlanRepository;
import com.huly.backend.domain.dto.payment.UserPlan;
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
    private UserPlanRepository userPlanRepository;
    private ListBackofficeUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(ExtensionSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        userPlanRepository = mock(UserPlanRepository.class);
        useCase = new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository, userPlanRepository);
    }

    @Test
    void execute_shouldProcessUsersAndSettingsWithoutHeavyMetrics() {
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

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings));
        when(userRepository.getCoins(2L)).thenReturn(50);
        when(userPlanRepository.findByUser(2L)).thenReturn(Optional.empty());

        List<BackofficeUserSummary> result = useCase.execute();

        assertThat(result).hasSize(1);
        BackofficeUserSummary summary = result.get(0);
        assertThat(summary.getId()).isEqualTo(2L);
        assertThat(summary.getName()).isEqualTo("John Doe");
        assertThat(summary.isAntiScrollEnabled()).isTrue();
        assertThat(summary.isDataSharingConsent()).isTrue();
        assertThat(summary.getCoins()).isEqualTo(50);
        assertThat(summary.getPlan()).isEqualTo("Gratuito");
        
        // Assert metrics are empty or null
        assertThat(summary.getMostUsedApp()).isNull();
        assertThat(summary.getMostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(summary.getTotalScrollTimeSeconds()).isEqualTo(0);
        assertThat(summary.getTopApps()).isEmpty();
        assertThat(summary.getDailyScrollTimeSeconds()).isEmpty();
        
        // Verify metricsRepository is not called
        verifyNoInteractions(metricsRepository);
    }
}
