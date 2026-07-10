package com.huly.backend.domain.useCase.admin.dashboard;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
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
class GetAdminDashboardUseCaseTest {

    private static final Long USER_1 = 2L;
    private static final Long USER_2 = 3L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private UserDetailDomainRepository userDetailRepository;

    @Mock
    private ActivitySessionRepository activitySessionRepository;

    @InjectMocks
    private GetAdminDashboardUseCase useCase;

    @Test
    @DisplayName("Agrega usuarios cuando hay datos")
    void executeShouldAggregateStatsWhenUsersExist() {
        // --- arrange ---
        givenNonAdmins(activeUser(USER_1), activeUser(USER_2));
        givenSettings(USER_1, settings(true));
        givenSettings(USER_2, settings(false));
        givenNoWeeklyData();

        // --- act ---
        GetAdminDashboardResponse stats = dashboard();

        // --- assert ---
        thenTwoUserAggregate(stats);
    }

    @Test
    @DisplayName("Ignora en los contadores a los usuarios sin configuración de anti-scroll")
    void executeShouldIgnoreUsersWithoutSettings() {
        // --- arrange ---
        givenNonAdmins(activeUser(USER_1));
        givenNoSettings(USER_1);
        givenNoWeeklyData();

        // --- act ---
        GetAdminDashboardResponse stats = dashboard();

        // --- assert ---
        thenUserCountedButNotActive(stats);
    }

    @Test
    @DisplayName("Devuelve estadísticas en cero cuando no hay usuarios")
    void executeShouldReturnZeroStatsWhenNoUsers() {
        // --- arrange ---
        givenNoNonAdmins();
        givenNoWeeklyData();

        // --- act ---
        GetAdminDashboardResponse stats = dashboard();

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

    private void givenNoWeeklyData() {
        lenient().when(userDetailRepository.findUserCreatedAt(anyLong())).thenReturn(Optional.empty());
        lenient().when(activitySessionRepository.findAllAfter(any())).thenReturn(List.of());
    }

    private AppUser activeUser(Long id) {
        return AppUser.builder().id(id).role(UserRole.USER).status(UserStatus.ACTIVE).build();
    }

    private UserAntiScrollSettings settings(boolean enabled) {
        return UserAntiScrollSettings.builder().enabled(enabled).build();
    }

    // --- act ---

    private GetAdminDashboardResponse dashboard() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenTwoUserAggregate(GetAdminDashboardResponse stats) {
        assertThat(stats.activeExtensionUsersCount()).isEqualTo(1);
        assertThat(stats.usersRegisteredThisWeek()).isEqualTo(0);
        assertThat(stats.activitiesThisWeek()).isEqualTo(0);
    }

    private void thenUserCountedButNotActive(GetAdminDashboardResponse stats) {
        assertThat(stats.activeExtensionUsersCount()).isEqualTo(0);
        assertThat(stats.usersRegisteredThisWeek()).isEqualTo(0);
        assertThat(stats.activitiesThisWeek()).isEqualTo(0);
    }

    private void thenZeroStats(GetAdminDashboardResponse stats) {
        assertThat(stats.activeExtensionUsersCount()).isEqualTo(0);
        assertThat(stats.usersRegisteredThisWeek()).isEqualTo(0);
        assertThat(stats.activitiesThisWeek()).isEqualTo(0);
    }
}
