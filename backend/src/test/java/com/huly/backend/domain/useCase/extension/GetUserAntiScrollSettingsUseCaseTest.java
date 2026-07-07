package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsRequest;
import com.huly.backend.domain.dto.extension.GetUserAntiScrollSettingsResponse;
import com.huly.backend.domain.mapper.extension.GetUserAntiScrollSettingsMapper;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserAntiScrollSettingsUseCaseTest {

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    @Mock
    private GetCurrentUserUseCase getCurrentUserUseCase;

    private GetUserAntiScrollSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserAntiScrollSettingsUseCase(
                settingsRepository,
                antiScrollConfigRepository,
                getCurrentUserUseCase,
                "http://localhost:5173",
                "http://localhost:8080",
                new GetUserAntiScrollSettingsMapper()
        );
    }

    @Test
    @DisplayName("Devuelve las opciones almacenadas cuando existen")
    void executeReturnsSettingsFromRepositoryWhenSettingsExist() {
        givenCurrentUserName("Jim");
        givenNoGlobalConfig();
        givenStoredSettings(1L, false, 30, List.of("twitter.com"), true);

        GetUserAntiScrollSettingsResponse result = getSettings(1L);

        thenReturnsStoredSettings(result);
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando no existen almacenadas")
    void executeReturnsDefaultSettingsWhenSettingsDoNotExist() {
        givenCurrentUserName("Jim");
        givenNoGlobalConfig();
        givenNoStoredSettings(2L);

        GetUserAntiScrollSettingsResponse result = getSettings(2L);

        thenReturnsStaticDefaults(result);
    }

    @Test
    @DisplayName("Devuelve defaults dinámicos cuando no hay almacenadas y existe configuración global")
    void executeReturnsDynamicDefaultSettingsWhenSettingsDoNotExistAndConfigExists() {
        givenCurrentUserName("Jim");
        givenGlobalConfig(35, "dynamic terms");
        givenNoStoredSettings(3L);

        GetUserAntiScrollSettingsResponse result = getSettings(3L);

        thenReturnsDynamicDefaults(result);
    }

    @Test
    @DisplayName("Usa los dominios por defecto cuando las opciones almacenadas no tienen dominios")
    void executeFallsBackToDefaultDomainsWhenStoredSettingsHaveEmptyDomains() {
        givenCurrentUserName("Jim");
        givenNoGlobalConfig();
        givenStoredSettings(4L, true, 45, List.of(), true);

        GetUserAntiScrollSettingsResponse result = getSettings(4L);

        thenFallsBackToDefaultDomains(result, 45);
    }

    @Test
    @DisplayName("Usa los dominios por defecto cuando las opciones almacenadas tienen dominios nulos")
    void executeFallsBackToDefaultDomainsWhenStoredSettingsHaveNullDomains() {
        givenCurrentUserName("Jim");
        givenNoGlobalConfig();
        givenStoredSettings(5L, true, 45, null, true);

        GetUserAntiScrollSettingsResponse result = getSettings(5L);

        thenFallsBackToDefaultDomains(result, 45);
    }

    // --- arrange ---

    private void givenCurrentUserName(String name) {
        AppUser user = AppUser.builder().id(1L).name(name).role(UserRole.USER).status(UserStatus.ACTIVE).build();
        when(getCurrentUserUseCase.execute(anyLong())).thenReturn(new UserProfile(user, true, true, true));
    }

    private void givenNoGlobalConfig() {
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    private void givenGlobalConfig(int defaultPauseIntervalMinutes, String termsAndConditions) {
        AntiScrollGlobalConfig config = AntiScrollGlobalConfig.builder()
                .defaultPauseIntervalMinutes(defaultPauseIntervalMinutes)
                .termsAndConditions(termsAndConditions)
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(config));
    }

    private void givenStoredSettings(long userId, boolean enabled, int pauseIntervalSeconds,
                                     List<String> monitoredDomains, boolean dataSharingConsent) {
        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(enabled)
                .pauseIntervalSeconds(pauseIntervalSeconds)
                .monitoredDomains(monitoredDomains)
                .dataSharingConsent(dataSharingConsent)
                .build();
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void givenNoStoredSettings(long userId) {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private GetUserAntiScrollSettingsResponse getSettings(long userId) {
        return useCase.execute(new GetUserAntiScrollSettingsRequest(userId));
    }

    // --- assert ---

    private void thenReturnsStoredSettings(GetUserAntiScrollSettingsResponse result) {
        assertThat(result).isNotNull();
        assertThat(result.enabled()).isFalse();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(30);
        assertThat(result.dataSharingConsent()).isTrue();
        assertThat(result.monitoredDomains()).containsExactly("twitter.com");
        assertThat(result.gardenUrl()).isEqualTo("http://localhost:5173/");
        assertThat(result.backendUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.userName()).isEqualTo("Jim");
    }

    private void thenReturnsStaticDefaults(GetUserAntiScrollSettingsResponse result) {
        assertThat(result).isNotNull();
        assertThat(result.enabled()).isTrue();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(1200);
        assertThat(result.dataSharingConsent()).isFalse();
        assertThat(result.gardenUrl()).isEqualTo("http://localhost:5173/");
        assertThat(result.backendUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.monitoredDomains()).contains("twitter.com", "x.com", "instagram.com");
    }

    private void thenReturnsDynamicDefaults(GetUserAntiScrollSettingsResponse result) {
        assertThat(result).isNotNull();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(2100);
        assertThat(result.termsAndConditions()).isEqualTo("dynamic terms");
    }

    private void thenFallsBackToDefaultDomains(GetUserAntiScrollSettingsResponse result, int expectedPauseIntervalSeconds) {
        assertThat(result).isNotNull();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(expectedPauseIntervalSeconds);
        assertThat(result.monitoredDomains())
                .containsExactly("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com");
    }
}
