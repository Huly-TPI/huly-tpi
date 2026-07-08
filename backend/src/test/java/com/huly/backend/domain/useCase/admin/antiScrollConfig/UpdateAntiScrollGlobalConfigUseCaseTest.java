package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAntiScrollGlobalConfigUseCaseTest {

    @Mock
    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    @InjectMocks
    private UpdateAntiScrollGlobalConfigUseCase useCase;

    @Test
    @DisplayName("Crea una configuración sin id cuando no existe una previa")
    void executeShouldCreateConfigWhenNoConfigExists() {
        // --- arrange ---
        givenNoExistingConfig();
        // --- act ---
        update(15, "nuevos terminos");
        // --- assert ---
        thenSavedConfigHasNoId(15, "nuevos terminos");
    }

    @Test
    @DisplayName("Conserva el id cuando ya existe una configuración")
    void executeShouldPreserveIdWhenConfigAlreadyExists() {
        // --- arrange ---
        givenExistingConfig(7L);
        // --- act ---
        update(30, "actualizados");
        // --- assert ---
        thenSavedConfigHasId(7L, 30, "actualizados");
    }

    // --- arrange ---

    private void givenNoExistingConfig() {
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    private void givenExistingConfig(Long id) {
        AntiScrollGlobalConfig existing = AntiScrollGlobalConfig.builder()
                .id(id)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("anteriores")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(existing));
    }

    // --- act ---

    private void update(int intervalMinutes, String terms) {
        useCase.execute(new UpdateAntiScrollGlobalConfigRequest(intervalMinutes, terms));
    }

    // --- assert ---

    private void thenSavedConfigHasNoId(int intervalMinutes, String terms) {
        AntiScrollGlobalConfig saved = captureSavedConfig();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getDefaultPauseIntervalMinutes()).isEqualTo(intervalMinutes);
        assertThat(saved.getTermsAndConditions()).isEqualTo(terms);
    }

    private void thenSavedConfigHasId(Long id, int intervalMinutes, String terms) {
        AntiScrollGlobalConfig saved = captureSavedConfig();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getDefaultPauseIntervalMinutes()).isEqualTo(intervalMinutes);
        assertThat(saved.getTermsAndConditions()).isEqualTo(terms);
    }

    private AntiScrollGlobalConfig captureSavedConfig() {
        ArgumentCaptor<AntiScrollGlobalConfig> captor = ArgumentCaptor.forClass(AntiScrollGlobalConfig.class);
        verify(antiScrollConfigRepository).save(captor.capture());
        return captor.getValue();
    }
}
