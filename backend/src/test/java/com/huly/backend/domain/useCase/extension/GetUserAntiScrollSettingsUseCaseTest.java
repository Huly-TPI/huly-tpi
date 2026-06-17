package com.huly.backend.domain.useCase.extension;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserAntiScrollSettingsUseCaseTest {

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    @Mock
    private GetCurrentUserUseCase getCurrentUserUseCase;

    private GetUserAntiScrollSettingsUseCase getUserAntiScrollSettingsUseCase;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());
        AppUser user = AppUser.builder().id(1L).name("Jim").role(UserRole.USER).status(UserStatus.ACTIVE).build();
        org.mockito.Mockito.lenient().when(getCurrentUserUseCase.execute(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new UserProfile(user, true, true, true));
        getUserAntiScrollSettingsUseCase = new GetUserAntiScrollSettingsUseCase(
                settingsRepository,
                antiScrollConfigRepository,
                getCurrentUserUseCase,
                "http://localhost:5173",
                "http://localhost:8080"
        );
    }

    @Test
    void execute_shouldReturnSettingsFromRepository_whenSettingsExist() {
        UserAntiScrollSettings existingSettings = UserAntiScrollSettings.builder()
                .enabled(false)
                .pauseIntervalSeconds(30)
                .monitoredDomains(List.of("twitter.com"))
                .dataSharingConsent(true)
                .build();

        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(existingSettings));

        GetUserAntiScrollSettingsResponse result = getUserAntiScrollSettingsUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.enabled()).isFalse();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(30);
        assertThat(result.dataSharingConsent()).isTrue();
        assertThat(result.monitoredDomains()).containsExactly("twitter.com");
        assertThat(result.gardenUrl()).isEqualTo("http://localhost:5173/");
        assertThat(result.backendUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.userName()).isEqualTo("Jim");
    }

    @Test
    void execute_shouldReturnDefaultSettings_whenSettingsDoNotExist() {
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.empty());

        GetUserAntiScrollSettingsResponse result = getUserAntiScrollSettingsUseCase.execute(2L);

        assertThat(result).isNotNull();
        assertThat(result.enabled()).isTrue();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(1200);
        assertThat(result.dataSharingConsent()).isFalse();
        assertThat(result.gardenUrl()).isEqualTo("http://localhost:5173/");
        assertThat(result.backendUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.monitoredDomains()).contains("twitter.com", "x.com", "instagram.com");
    }

    @Test
    void execute_shouldReturnDynamicDefaultSettings_whenSettingsDoNotExistAndConfigExists() {
        AntiScrollGlobalConfig config = AntiScrollGlobalConfig.builder()
                .defaultPauseIntervalMinutes(35)
                .termsAndConditions("dynamic terms")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(config));
        when(settingsRepository.findByUserId(3L)).thenReturn(Optional.empty());

        GetUserAntiScrollSettingsResponse result = getUserAntiScrollSettingsUseCase.execute(3L);

        assertThat(result).isNotNull();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(2100);
        assertThat(result.termsAndConditions()).isEqualTo("dynamic terms");
    }

    @Test
    void execute_shouldFallbackToDefaultDomains_whenStoredSettingsHaveNoDomains() {
        UserAntiScrollSettings existingSettings = UserAntiScrollSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(45)
                .monitoredDomains(List.of())
                .dataSharingConsent(true)
                .build();

        when(settingsRepository.findByUserId(4L)).thenReturn(Optional.of(existingSettings));

        GetUserAntiScrollSettingsResponse result = getUserAntiScrollSettingsUseCase.execute(4L);

        assertThat(result).isNotNull();
        assertThat(result.pauseIntervalSeconds()).isEqualTo(45);
        assertThat(result.monitoredDomains())
                .containsExactly("twitter.com", "x.com", "instagram.com", "tiktok.com", "youtube.com", "facebook.com");
    }
}
