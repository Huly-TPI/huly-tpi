package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserSettingEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserSettingJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAntiScrollSettingsRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final Long MISSING_USER_ID = 99L;

    @Mock
    private IUserSettingJpaRepository userSettingJpaRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserAntiScrollSettingsRepositoryImpl repository;

    @Test
    @DisplayName("Mapea la configuración cuando existe con campos no nulos")
    void findByUserIdShouldReturnMappedSettingsWhenFoundWithNonNullFields() {
        givenSettingsFound(settingsEntityWithNonNullFields());

        Optional<UserAntiScrollSettings> result = findByUserId(USER_ID);

        thenMappedSettingsWithNonNullFields(result);
    }

    @Test
    @DisplayName("Aplica valores por defecto cuando los campos son nulos")
    void findByUserIdShouldReturnSettingsWithDefaultFallbackValuesWhenFoundWithNullFields() {
        givenSettingsFound(settingsEntityWithNullFields());

        Optional<UserAntiScrollSettings> result = findByUserId(USER_ID);

        thenSettingsWithDefaultFallbacks(result);
    }

    @Test
    @DisplayName("Usa los minutos heredados cuando la columna de segundos es nula")
    void findByUserIdShouldFallbackToLegacyMinutesWhenSecondsColumnIsNull() {
        givenSettingsFound(settingsEntityWithLegacyMinutes());

        Optional<UserAntiScrollSettings> result = findByUserId(USER_ID);

        thenPauseIntervalSecondsFromLegacyMinutes(result);
    }

    @Test
    @DisplayName("Aplica dominios por defecto cuando la cadena de dominios está vacía")
    void findByUserIdShouldReturnSettingsWithDefaultDomainsWhenDomainsIsEmptyString() {
        givenSettingsFound(settingsEntityWithBlankDomains());

        Optional<UserAntiScrollSettings> result = findByUserId(USER_ID);

        thenSettingsWithDefaultDomains(result);
    }

    @Test
    @DisplayName("Devuelve vacío cuando no se encuentra la configuración")
    void findByUserIdShouldReturnEmptyWhenNotFound() {
        givenNoSettingsForUser(MISSING_USER_ID);

        Optional<UserAntiScrollSettings> result = findByUserId(MISSING_USER_ID);

        thenAbsent(result);
    }

    @Test
    @DisplayName("Actualiza la configuración cuando ya existe")
    void saveShouldUpdateSettingsWhenSettingsAlreadyExist() {
        UserSettingEntity existing = existingSettingsSpy();
        givenSettingsFound(existing);

        save(settingsToUpdate());

        thenExistingSettingsUpdated(existing);
    }

    @Test
    @DisplayName("Crea la configuración cuando no existe")
    void saveShouldCreateSettingsWhenSettingsDoNotExist() {
        AppUserEntity user = referencedUser();
        givenNoSettingsForUser(USER_ID);
        givenReferencedUser(user);

        save(settingsToCreate());

        thenCreatedSettingsPersisted(user);
    }

    // --- arrange ---
    private void givenSettingsFound(UserSettingEntity entity) {
        when(userSettingJpaRepository.findByAppUser_Id(USER_ID)).thenReturn(Optional.of(entity));
    }

    private void givenNoSettingsForUser(Long userId) {
        when(userSettingJpaRepository.findByAppUser_Id(userId)).thenReturn(Optional.empty());
    }

    private void givenReferencedUser(AppUserEntity user) {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(user);
    }

    private UserSettingEntity settingsEntityWithNonNullFields() {
        return UserSettingEntity.builder()
                .antiScrollEnabled(true)
                .pauseIntervalSeconds(30)
                .monitoredDomains("youtube.com, tiktok.com, , x.com")
                .dataSharingConsent(true)
                .build();
    }

    private UserSettingEntity settingsEntityWithNullFields() {
        return UserSettingEntity.builder()
                .antiScrollEnabled(null)
                .pauseIntervalMinutes(null)
                .pauseIntervalSeconds(null)
                .monitoredDomains(null)
                .dataSharingConsent(null)
                .build();
    }

    private UserSettingEntity settingsEntityWithLegacyMinutes() {
        return UserSettingEntity.builder()
                .pauseIntervalMinutes(12)
                .pauseIntervalSeconds(null)
                .build();
    }

    private UserSettingEntity settingsEntityWithBlankDomains() {
        return UserSettingEntity.builder()
                .monitoredDomains("   ")
                .build();
    }

    private UserSettingEntity existingSettingsSpy() {
        return spy(UserSettingEntity.builder()
                .antiScrollEnabled(false)
                .pauseIntervalMinutes(15)
                .pauseIntervalSeconds(900)
                .monitoredDomains("instagram.com")
                .dataSharingConsent(false)
                .build());
    }

    private AppUserEntity referencedUser() {
        return AppUserEntity.builder().id(USER_ID).build();
    }

    private UserAntiScrollSettings settingsToUpdate() {
        return UserAntiScrollSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(45)
                .monitoredDomains(List.of("twitter.com", "reddit.com"))
                .dataSharingConsent(true)
                .build();
    }

    private UserAntiScrollSettings settingsToCreate() {
        return UserAntiScrollSettings.builder()
                .enabled(true)
                .pauseIntervalSeconds(35)
                .monitoredDomains(null)
                .dataSharingConsent(false)
                .build();
    }

    // --- act ---
    private Optional<UserAntiScrollSettings> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    private void save(UserAntiScrollSettings settings) {
        repository.save(USER_ID, settings);
    }

    // --- assert ---
    private void thenMappedSettingsWithNonNullFields(Optional<UserAntiScrollSettings> result) {
        assertThat(result).isPresent();
        UserAntiScrollSettings settings = result.get();
        assertThat(settings.isEnabled()).isTrue();
        assertThat(settings.getPauseIntervalSeconds()).isEqualTo(30);
        assertThat(settings.getMonitoredDomains()).containsExactly("youtube.com", "tiktok.com", "x.com");
        assertThat(settings.isDataSharingConsent()).isTrue();
    }

    private void thenSettingsWithDefaultFallbacks(Optional<UserAntiScrollSettings> result) {
        assertThat(result).isPresent();
        UserAntiScrollSettings settings = result.get();
        assertThat(settings.isEnabled()).isTrue(); // Default value fallback
        assertThat(settings.getPauseIntervalSeconds()).isEqualTo(1200); // Default value fallback
        assertThat(settings.getMonitoredDomains()).hasSize(6); // Default list fallback
        assertThat(settings.isDataSharingConsent()).isFalse(); // Default value fallback
    }

    private void thenPauseIntervalSecondsFromLegacyMinutes(Optional<UserAntiScrollSettings> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getPauseIntervalSeconds()).isEqualTo(720);
    }

    private void thenSettingsWithDefaultDomains(Optional<UserAntiScrollSettings> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getMonitoredDomains()).hasSize(6); // Default list fallback
    }

    private void thenAbsent(Optional<UserAntiScrollSettings> result) {
        assertThat(result).isEmpty();
    }

    private void thenExistingSettingsUpdated(UserSettingEntity existing) {
        verify(existing).setAntiScrollEnabled(true);
        verify(existing).setPauseIntervalSeconds(45);
        verify(existing).setPauseIntervalMinutes(0);
        verify(existing).setDataSharingConsent(true);
        verify(existing).setMonitoredDomains("twitter.com,reddit.com");
        verify(userSettingJpaRepository).save(existing);
    }

    private void thenCreatedSettingsPersisted(AppUserEntity user) {
        ArgumentCaptor<UserSettingEntity> captor = ArgumentCaptor.forClass(UserSettingEntity.class);
        verify(userSettingJpaRepository).save(captor.capture());
        UserSettingEntity saved = captor.getValue();
        assertThat(saved.getAppUser()).isEqualTo(user);
        assertThat(saved.getAntiScrollEnabled()).isTrue();
        assertThat(saved.getPauseIntervalSeconds()).isEqualTo(35);
        assertThat(saved.getPauseIntervalMinutes()).isEqualTo(0);
        assertThat(saved.getMonitoredDomains()).isNull();
        assertThat(saved.getDataSharingConsent()).isFalse();
    }
}
