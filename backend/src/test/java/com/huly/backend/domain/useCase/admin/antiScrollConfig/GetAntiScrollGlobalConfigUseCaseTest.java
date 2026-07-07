package com.huly.backend.domain.useCase.admin.antiScrollConfig;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.domain.repository.extension.AntiScrollGlobalConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAntiScrollGlobalConfigUseCaseTest {

    private static final String DEFAULT_TERMS =
            "El modo anti-scroll es simplemente una herramienta para acompañarte cuando sientas que necesitás frenar un poco. No hay reglas estrictas ni metas que cumplir. Activalo cuando quieras priorizar tu concentración o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. ¡Cero presiones, el ritmo lo marcás vos!";

    @Mock
    private AntiScrollGlobalConfigRepository antiScrollConfigRepository;

    @InjectMocks
    private GetAntiScrollGlobalConfigUseCase useCase;

    @Test
    @DisplayName("Devuelve la configuración almacenada cuando existe")
    void executeShouldReturnStoredConfigWhenConfigExists() {
        // --- arrange ---
        givenStoredConfig();
        // --- act ---
        GetAntiScrollGlobalConfigResponse response = getConfig();
        // --- assert ---
        thenResponseIs(response, 25, "terminos guardados");
    }

    @Test
    @DisplayName("Devuelve la configuración por defecto cuando no existe")
    void executeShouldReturnDefaultConfigWhenConfigDoesNotExist() {
        // --- arrange ---
        givenNoStoredConfig();
        // --- act ---
        GetAntiScrollGlobalConfigResponse response = getConfig();
        // --- assert ---
        thenResponseIs(response, 20, DEFAULT_TERMS);
    }

    // --- arrange ---

    private void givenStoredConfig() {
        AntiScrollGlobalConfig config = AntiScrollGlobalConfig.builder()
                .id(1L)
                .defaultPauseIntervalMinutes(25)
                .termsAndConditions("terminos guardados")
                .build();
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.of(config));
    }

    private void givenNoStoredConfig() {
        when(antiScrollConfigRepository.findFirst()).thenReturn(Optional.empty());
    }

    // --- act ---

    private GetAntiScrollGlobalConfigResponse getConfig() {
        return useCase.execute();
    }

    // --- assert ---

    private void thenResponseIs(GetAntiScrollGlobalConfigResponse response, int intervalMinutes, String terms) {
        assertThat(response.defaultPauseIntervalMinutes()).isEqualTo(intervalMinutes);
        assertThat(response.termsAndConditions()).isEqualTo(terms);
    }
}
