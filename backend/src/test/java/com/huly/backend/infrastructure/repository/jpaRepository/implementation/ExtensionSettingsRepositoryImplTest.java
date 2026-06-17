package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.ExtensionSettings;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserSettingEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserSettingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtensionSettingsRepositoryImplTest {

    @Mock
    private IUserSettingJpaRepository userSettingJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ExtensionSettingsRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(repository, "frontendUrl", "http://frontend.com");
        ReflectionTestUtils.setField(repository, "backendUrl", "http://backend.com");
    }

    @Test
    void findByUserId_shouldReturnMappedSettings_whenFoundWithNonNullFields() {
        Long userId = 1L;
        UserSettingEntity entity = UserSettingEntity.builder()
                .antiScrollEnabled(true)
                .pauseIntervalSeconds(30)
                .monitoredDomains("youtube.com, tiktok.com, , x.com")
                .dataSharingConsent(true)
                .build();

        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.of(entity));

        Optional<ExtensionSettings> result = repository.findByUserId(userId);

        assertThat(result).isPresent();
        ExtensionSettings settings = result.get();
        assertThat(settings.isEnabled()).isTrue();
        assertThat(settings.getPauseIntervalSeconds()).isEqualTo(30);
        assertThat(settings.getGardenUrl()).isEqualTo("http://frontend.com/");
        assertThat(settings.getBackendUrl()).isEqualTo("http://backend.com");
        assertThat(settings.getMonitoredDomains()).containsExactly("youtube.com", "tiktok.com", "x.com");
        assertThat(settings.isDataSharingConsent()).isTrue();
    }

    @Test
    void findByUserId_shouldReturnSettingsWithDefaultFallbackValues_whenFoundWithNullFields() {
        Long userId = 1L;
        UserSettingEntity entity = UserSettingEntity.builder()
                .antiScrollEnabled(null)
                .pauseIntervalMinutes(null)
                .pauseIntervalSeconds(null)
                .monitoredDomains(null)
                .dataSharingConsent(null)
                .build();

        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.of(entity));

        Optional<ExtensionSettings> result = repository.findByUserId(userId);

        assertThat(result).isPresent();
        ExtensionSettings settings = result.get();
        assertThat(settings.isEnabled()).isTrue(); // Default value fallback
        assertThat(settings.getPauseIntervalSeconds()).isEqualTo(1200); // Default value fallback
        assertThat(settings.getMonitoredDomains()).hasSize(6); // Default list fallback
        assertThat(settings.isDataSharingConsent()).isFalse(); // Default value fallback
    }

    @Test
    void findByUserId_shouldFallbackToLegacyMinutes_whenSecondsColumnIsNull() {
        Long userId = 1L;
        UserSettingEntity entity = UserSettingEntity.builder()
                .pauseIntervalMinutes(12)
                .pauseIntervalSeconds(null)
                .build();

        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.of(entity));

        Optional<ExtensionSettings> result = repository.findByUserId(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getPauseIntervalSeconds()).isEqualTo(720);
    }

    @Test
    void findByUserId_shouldReturnSettingsWithDefaultDomains_whenDomainsIsEmptyString() {
        Long userId = 1L;
        UserSettingEntity entity = UserSettingEntity.builder()
                .monitoredDomains("   ")
                .build();

        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.of(entity));

        Optional<ExtensionSettings> result = repository.findByUserId(userId);

        assertThat(result).isPresent();
        ExtensionSettings settings = result.get();
        assertThat(settings.getMonitoredDomains()).hasSize(6); // Default list fallback
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenNotFound() {
        Long userId = 99L;
        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.empty());

        assertThat(repository.findByUserId(userId)).isEmpty();
    }

    @Test
    void save_shouldUpdateSettings_whenSettingsAlreadyExist() {
        Long userId = 1L;
        UserSettingEntity existingEntity = spy(UserSettingEntity.builder()
                .antiScrollEnabled(false)
                .pauseIntervalMinutes(15)
                .pauseIntervalSeconds(900)
                .monitoredDomains("instagram.com")
                .dataSharingConsent(false)
                .build());

        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.of(existingEntity));

        ExtensionSettings settingsToSave = ExtensionSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(45)
                .monitoredDomains(List.of("twitter.com", "reddit.com"))
                .dataSharingConsent(true)
                .build();

        repository.save(userId, settingsToSave);

        verify(existingEntity).setAntiScrollEnabled(true);
        verify(existingEntity).setPauseIntervalSeconds(45);
        verify(existingEntity).setPauseIntervalMinutes(0);
        verify(existingEntity).setDataSharingConsent(true);
        verify(existingEntity).setMonitoredDomains("twitter.com,reddit.com");
        verify(userSettingJpaRepository).save(existingEntity);
    }

    @Test
    void save_shouldCreateSettings_whenSettingsDoNotExist() {
        Long userId = 1L;
        AppUserEntity mockUser = AppUserEntity.builder().id(userId).build();
        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.empty());
        when(appUserRepository.getReferenceById(userId)).thenReturn(mockUser);

        ExtensionSettings settingsToSave = ExtensionSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(35)
                .monitoredDomains(null)
                .dataSharingConsent(false)
                .build();

        repository.save(userId, settingsToSave);

        ArgumentCaptor<UserSettingEntity> captor = ArgumentCaptor.forClass(UserSettingEntity.class);
        verify(userSettingJpaRepository).save(captor.capture());

        UserSettingEntity saved = captor.getValue();
        assertThat(saved.getAppUser()).isEqualTo(mockUser);
        assertThat(saved.getAntiScrollEnabled()).isTrue();
        assertThat(saved.getPauseIntervalSeconds()).isEqualTo(35);
        assertThat(saved.getPauseIntervalMinutes()).isEqualTo(0);
        assertThat(saved.getMonitoredDomains()).isNull();
        assertThat(saved.getDataSharingConsent()).isFalse();
    }
}
