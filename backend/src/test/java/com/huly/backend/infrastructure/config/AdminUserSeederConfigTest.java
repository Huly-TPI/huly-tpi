package com.huly.backend.infrastructure.config;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserSeederConfigTest {

    private static final String ADMIN_EMAIL = "admin@huly.com";
    private static final String RAW_PASSWORD = "admin123";
    private static final String ENCODED_PASSWORD = "encoded-admin123";

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final AdminUserSeederConfig config = new AdminUserSeederConfig();

    @Test
    @DisplayName("No crea ni guarda el admin cuando ya existe")
    void createAdminUserShouldNotSaveWhenAdminAlreadyExists() throws Exception {
        // --- arrange ---
        givenAdminAlreadyExists();

        // --- act ---
        runSeeder();

        // --- assert ---
        thenNoUserWasSaved();
    }

    @Test
    @DisplayName("Crea y guarda el admin con los atributos esperados cuando no existe")
    void createAdminUserShouldCreateAndSaveWhenAdminDoesNotExist() throws Exception {
        // --- arrange ---
        givenAdminDoesNotExist();
        givenEncodedPassword();

        // --- act ---
        runSeeder();

        // --- assert ---
        thenAdminWasSavedWithExpectedAttributes();
    }

    // --- arrange ---

    private void givenAdminAlreadyExists() {
        when(appUserRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(true);
    }

    private void givenAdminDoesNotExist() {
        when(appUserRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(false);
    }

    private void givenEncodedPassword() {
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
    }

    // --- act ---

    private void runSeeder() throws Exception {
        config.createAdminUser(appUserRepository, passwordEncoder).run();
    }

    // --- assert ---

    private void thenNoUserWasSaved() {
        verify(appUserRepository, never()).save(any());
    }

    private void thenAdminWasSavedWithExpectedAttributes() {
        ArgumentCaptor<AppUserEntity> captor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository).save(captor.capture());
        AppUserEntity saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(saved.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getCoins()).isEqualTo(0);
    }
}
